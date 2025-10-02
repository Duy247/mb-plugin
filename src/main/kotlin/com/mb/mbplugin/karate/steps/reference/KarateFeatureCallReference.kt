package com.mb.mbplugin.karate.steps.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.mb.mbplugin.karate.index.KarateFeatureIndex
import com.mb.mbplugin.karate.psi.KarateFile
import com.mb.mbplugin.karate.psi.KaratePsiElement
import com.mb.mbplugin.karate.psi.KarateTokenTypes
import java.util.regex.Pattern

class KarateFeatureCallReference(
    element: PsiElement,
    textRange: TextRange
) : PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {

    companion object {
        private val CALL_READ_PATTERN = Pattern.compile(
            "call\\s+read\\s*\\(\\s*['\"]classpath:([^'\"]+\\.feature)(@(\\w+))?['\"]",
            Pattern.CASE_INSENSITIVE
        )
    }

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val elementText = element.text
        val matcher = CALL_READ_PATTERN.matcher(elementText)
        
        if (!matcher.find()) {
            return emptyArray()
        }
        
        val featureFilePath = matcher.group(1)
        val scenarioTag = matcher.group(3) // Group 3 because group 2 includes the @
        
        return findFeatureFileAndScenario(featureFilePath, scenarioTag)
    }

    private fun findFeatureFileAndScenario(featureFilePath: String, scenarioTag: String?): Array<ResolveResult> {
        val results = mutableListOf<ResolveResult>()
        val project = element.project
        
        // Extract filename from path
        val fileName = featureFilePath.substringAfterLast("/")
        
        // Get all feature files and filter by name
        val allFeatureFiles = KarateFeatureIndex.getAllFeatureFiles(project)
        val matchingFiles = allFeatureFiles.filter { virtualFile ->
            virtualFile.name == fileName
        }
        
        for (virtualFile in matchingFiles) {
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile is KarateFile) {
                // If a specific scenario tag is specified, try to find it
                if (scenarioTag != null) {
                    val scenario = findScenarioWithTag(psiFile, scenarioTag)
                    if (scenario != null) {
                        results.add(createResolveResult(scenario))
                    }
                } else {
                    // If no specific scenario, return the file itself
                    results.add(createResolveResult(psiFile))
                }
                
                // Also add the file as a fallback option
                if (scenarioTag != null) {
                    results.add(createResolveResult(psiFile))
                }
            }
        }
        
        return results.toTypedArray()
    }

    private fun findScenarioWithTag(karateFile: KarateFile, scenarioTag: String): PsiElement? {
        // Look for scenarios with the specified tag
        val scenarios = PsiTreeUtil.findChildrenOfType(karateFile, KaratePsiElement::class.java)
            .filter { 
                it.node.elementType == KarateTokenTypes.SCENARIO_KEYWORD ||
                it.node.elementType == KarateTokenTypes.SCENARIO_OUTLINE_KEYWORD
            }
        
        for (scenario in scenarios) {
            // Check if this scenario has the matching tag
            val scenarioText = scenario.text
            if (scenarioText.contains("@$scenarioTag")) {
                return scenario
            }
            
            // Also check if the scenario name matches (case-insensitive)
            if (scenarioText.lowercase().contains(scenarioTag.lowercase())) {
                return scenario
            }
        }
        
        // Look for tags in the file
        val tags = PsiTreeUtil.findChildrenOfType(karateFile, KaratePsiElement::class.java)
            .filter { it.node.elementType == KarateTokenTypes.TAG }
        
        for (tag in tags) {
            if (tag.text.contains("@$scenarioTag")) {
                // Find the next scenario after this tag
                var next = tag.nextSibling
                while (next != null) {
                    if (next is KaratePsiElement && 
                        (next.node.elementType == KarateTokenTypes.SCENARIO_KEYWORD ||
                         next.node.elementType == KarateTokenTypes.SCENARIO_OUTLINE_KEYWORD)) {
                        return next
                    }
                    next = next.nextSibling
                }
            }
        }
        
        return null
    }

    private fun createResolveResult(element: PsiElement): ResolveResult {
        return object : ResolveResult {
            override fun getElement(): PsiElement = element
            override fun isValidResult(): Boolean = true
        }
    }

    override fun getVariants(): Array<Any> {
        // Could be extended to provide completion variants for feature files
        return emptyArray()
    }
}