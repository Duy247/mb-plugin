package com.mb.mbplugin.karate.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.mb.mbplugin.karate.psi.GherkinFileType
import java.io.DataInput
import java.io.DataOutput

/**
 * Index for Karate feature files and their scenario tags
 */
class KarateFeatureIndex : FileBasedIndexExtension<String, KarateFeatureData>() {
    
    companion object {
        val NAME = ID.create<String, KarateFeatureData>("KarateFeatureIndex")
        
        fun getAllFeatureFiles(project: Project): Collection<VirtualFile> {
            return FileTypeIndex.getFiles(GherkinFileType.INSTANCE, GlobalSearchScope.projectScope(project))
        }
        
        fun getFeatureData(project: Project, tag: String): List<KarateFeatureData> {
            val result = mutableListOf<KarateFeatureData>()
            FileBasedIndex.getInstance().processValues(
                NAME, tag, null,
                { _, value ->
                    result.add(value)
                    true
                },
                GlobalSearchScope.projectScope(project)
            )
            return result
        }
    }
    
    override fun getName(): ID<String, KarateFeatureData> = NAME
    
    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE
    
    override fun getValueExternalizer(): DataExternalizer<KarateFeatureData> = KarateFeatureDataExternalizer()
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(GherkinFileType.INSTANCE) {
            override fun acceptInput(file: VirtualFile): Boolean {
                return file.extension == "feature"
            }
        }
    }
    
    override fun dependsOnFileContent(): Boolean = true
    
    override fun getVersion(): Int = 1
    
    override fun getIndexer(): DataIndexer<String, KarateFeatureData, FileContent> {
        return DataIndexer { inputData ->
            val result = mutableMapOf<String, KarateFeatureData>()
            
            try {
                val project = inputData.project
                val file = inputData.file
                val psiFile = PsiManager.getInstance(project).findFile(file)
                
                if (psiFile != null) {
                    val tags = extractTagsFromFile(psiFile)
                    val relativePath = getRelativePathFromSourceRoot(project, file)
                    
                    tags.forEach { tag ->
                        result[tag] = KarateFeatureData(relativePath, tag, file.path)
                    }
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
            
            result
        }
    }
    
    private fun extractTagsFromFile(psiFile: PsiFile): List<String> {
        val tags = mutableListOf<String>()
        val text = psiFile.text
        
        // Simple regex to find tags like @login, @smoke, etc.
        val tagPattern = Regex("@([a-zA-Z_][a-zA-Z0-9_]*)")
        tagPattern.findAll(text).forEach { match ->
            tags.add(match.groupValues[1]) // Add without @ symbol
        }
        
        return tags.distinct()
    }
    
    private fun getRelativePathFromSourceRoot(project: Project, file: VirtualFile): String {
        // Find the relative path from source root
        val projectPath = project.basePath ?: return file.path
        val filePath = file.path
        
        // Common source root patterns
        val sourceRoots = listOf("src/test/java", "src/main/java", "src/test/resources", "src/main/resources")
        
        for (sourceRoot in sourceRoots) {
            val sourceRootPath = "$projectPath/$sourceRoot".replace("/", java.io.File.separator)
            if (filePath.startsWith(sourceRootPath)) {
                // Return path after the source root (this is the classpath)
                return filePath.substring(sourceRootPath.length + 1)
                    .replace("\\", "/")
                    .replace(java.io.File.separator, "/")
            }
        }
        
        // Try to find using module source roots from IntelliJ
        try {
            val moduleManager = ModuleManager.getInstance(project)
            val modules = moduleManager.modules
            for (module in modules) {
                val moduleRootManager = ModuleRootManager.getInstance(module)
                
                // Check all source roots
                val sourceRoots = moduleRootManager.sourceRoots
                for (sourceRoot in sourceRoots) {
                    if (filePath.startsWith(sourceRoot.path)) {
                        return filePath.substring(sourceRoot.path.length + 1)
                            .replace("\\", "/")
                            .replace(java.io.File.separator, "/")
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore module resolution errors
        }
        
        // Fallback: relative to project root
        return if (filePath.startsWith(projectPath)) {
            filePath.substring(projectPath.length + 1).replace("\\", "/")
        } else {
            filePath
        }
    }
}

/**
 * Data class to hold feature file information
 */
data class KarateFeatureData(
    val relativePath: String,
    val tag: String,
    val absolutePath: String
)

/**
 * Externalizer for KarateFeatureData
 */
class KarateFeatureDataExternalizer : DataExternalizer<KarateFeatureData> {
    override fun save(out: DataOutput, value: KarateFeatureData) {
        out.writeUTF(value.relativePath)
        out.writeUTF(value.tag)
        out.writeUTF(value.absolutePath)
    }
    
    override fun read(input: DataInput): KarateFeatureData {
        val relativePath = input.readUTF()
        val tag = input.readUTF()
        val absolutePath = input.readUTF()
        return KarateFeatureData(relativePath, tag, absolutePath)
    }
}