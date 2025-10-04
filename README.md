# README

Đây là một chia sẻ nhỏ, không phải là hướng dẫn làm thế nào để compile ra plugin, làm thế nào để xử lý dependency của plugin, làm thế nào để maintain, upgrade,... mà là một chia sẻ về lý do plugin này được tạo ra.

## Về Karate Framework
Karate Framework là một công cụ mạnh để viết test tự động cho API và UI. Tuy nhiên, khi đi sâu sử dụng trong dự án thực tế, chúng ta đều thấy rằng nó cũng không hề dễ dàng:
- Cấu trúc feature file nhiều khi phức tạp, dễ gây nhầm lẫn do dựa vào nhiều `* call read` để tái sử dụng Scenario
- Các Scenario vốn không phải code, nên sẽ không được index để đưa vào tính năng gợi ý, tự hoàn thiện code

## Vấn đề với Gherkin “Go To Definition”
Một trong những hạn chế rõ rệt là tính năng “Go to Definition” trong IntelliJ đối với Gherkin/Karate:
- Với step định nghĩa trong Java/Cucumber, IntelliJ hỗ trợ khá ổn, Ctrl + Click sẽ hướng ngay đến method Java dùng để định nghĩa step
- Nhưng với Karate feature file, đặc biệt khi dùng `call read`, việc điều hướng hầu như bị “gãy”
- Điều này làm cho việc đọc hiểu luồng test, tái sử dụng scenario và maintain, debug trở nên mất thời gian, hoặc phải tìm kiếm bằng tay, hoặc Ctrl Shift F và mong phép màu

Đó cũng chính là động lực để tôi viết thêm plugin này — nhằm vá những điểm thiếu hụt, để công việc bớt nặng nề.

## Tinh thần làm việc 
**Không nên chỉ “chờ sung”.**

Trong team Auto Tester, đôi khi chúng ta rơi vào thói quen **dựa hoàn toàn vào công cụ sẵn có**. Khi gặp giới hạn về mặt kỹ thuật, nhiều lúc ta chọn cách… dừng lại và chờ “sung rụng” thay vì chủ động tìm cách vượt qua. Một ví dụ cụ thể, đó là tại thời điểm hiện tại, rất nhiều cá nhân đang kẹt lại ở IntelliJ IDEA 2023.2 và không hề config proxy để sử dụng các plugin bên ngoài (mà lại đi chia sẻ plugin bản zip và jar để cài từ local ?)


Điều đó làm mất đi rất nhiều cơ hội để cải tiến năng suất. Bởi lẽ:
- Công cụ nào cũng có giới hạn
- Chúng ta chính là người sử dụng thực tế, hiểu rõ nhất những điểm vướng, nhưng không tìm tòi, cố gắng giải quyết thì những yêu cầu cải tiến sẽ không thể được chứng minh
- Nếu không chủ động bổ sung, thì chính mình sẽ mãi bị giới hạn bởi công cụ, những người có thẩm quyền quyết định các công cụ có thể sử dụng lại không biết về khó khăn chúng ta hay gặp

## Chúng ta nên làm gì?
- **Tận dụng tất cả những gì có thể**: IntelliJ, plugin, script, custom hook, bất cứ giải pháp nào, cái gì cũng được, **miễn** là giải quyết được vấn đề
- **Chủ động cải tiến**: Thấy vướng thì nghĩ cách vượt qua, chứ không chờ ai khác
- **Chia sẻ & đóng góp**: Dù là một snippet, một plugin nhỏ hay một mẹo debug, đều có thể giúp cả team tiết kiệm hàng giờ

---

✦ Đây chính là lý do mà plugin này được viết ra: không phải để “làm màu”, mà như là một minh chứng rằng chúng ta hoàn toàn có thể **tự nâng cấp công cụ** và tự nâng cao hiệu suất làm việc. **Không cần phải dựa dẫm vào ai khác**
