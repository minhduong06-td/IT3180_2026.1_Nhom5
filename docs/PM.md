# Giải trình dành cho PM

## Sản phẩm giải quyết việc gì?

BlueMoon hỗ trợ ban quản trị lập khoản phải thu theo tháng và kế toán ghi nhận tiền cư dân nộp. Dữ liệu đi theo luồng: căn hộ và cư dân → khoản thu và đơn giá → kỳ thu → bảng kê → thanh toán → công nợ. Phương tiện, tiện ích và đóng góp bổ sung cho luồng này.

Phân công của nhóm trên quy trình làm việc: dev thực hiện thay đổi trên nhánh, QA kiểm tra và cung cấp kết quả, PM đối chiếu yêu cầu và quyết định tích hợp. QA không cần commit để cung cấp bằng chứng.

## Tại sao cần nhánh, PR và merge commit?

Mỗi nhánh gom một thay đổi có mục đích rõ ràng. PR cho người review thấy vấn đề, cách xử lý, phần bị ảnh hưởng và bằng chứng kiểm tra. Merge commit giữ lại ranh giới giữa các thay đổi để lần theo lỗi hoặc hoàn tác cả thay đổi khi cần.

Lịch sử nhánh và commit không tự chứng minh có nhiều người review. Khi cùng một tài khoản thực hiện code và merge, cần nói đúng đó là tự review; không ghi thành phê duyệt độc lập của PM hoặc QA.

## Tại sao merge thay đổi khởi động với database rỗng?

Bootstrap chỉ tạo và nâng cấp schema. Dữ liệu mẫu do Docker nạp từ `database/demo-data.sql` khi tạo volume mới. Cách tách này tránh trường hợp dữ liệu cố định không đủ mã căn mà bước thêm cư dân yêu cầu, khiến ứng dụng báo lỗi khởi động.

Nếu chưa có user, ứng dụng cho tạo quản trị đầu tiên; nếu đã có user thì đăng nhập. Người clone repo không phải nhập dữ liệu nghiệp vụ có sẵn.

Căn cứ kỹ thuật đã ghi ở [biên bản QA](QA_TEST_REPORT.md): các lượt kiểm tra với JDK 25 và MySQL 8.4 có 14 test đạt trên schema mới và schema cũ có ba căn. Với chế độ legacy, test tích hợp chỉ đi qua nhánh nâng cấp và tạo quản trị, không chạy toàn bộ luồng tính phí. JavaFX khởi động được với database trống; thao tác trực tiếp trên màn hình chưa được xác nhận.

Cách khởi tạo mới không đóng các lỗi tài chính còn mở và không đồng nghĩa toàn bộ sản phẩm được nghiệm thu.

## Vì sao cần tinh gọn giao diện?

Màn hình làm việc cần giúp người dùng tìm hồ sơ, kiểm tra số tiền và thực hiện thao tác. Slogan ở màn hình đăng nhập và phụ đề lặp lại tên trang chiếm chỗ nhưng không hỗ trợ các quyết định đó. Giao diện giữ lại hướng dẫn nhập liệu, quy tắc phát hành, cảnh báo và các con số cần đối soát.

Menu cuộn và thanh công cụ xuống dòng giúp các thao tác vẫn truy cập được ở cửa sổ nhỏ. Khi review, PM cần xem cả ảnh render và khả năng sử dụng thật; việc bỏ chữ chỉ hợp lý nếu người dùng vẫn hiểu trạng thái và hành động tiếp theo.

## PM cần đọc gì trước khi merge một PR?

- Vấn đề có đối chiếu được với yêu cầu hoặc lỗi cụ thể không?
- Phạm vi diff có đúng mục đích nhánh, có thay đổi ngoài yêu cầu không?
- Quyền ở service, transaction và dữ liệu lịch sử có bị ảnh hưởng không?
- Test nào đã chạy trên commit nào, có test bị bỏ qua không?
- Có thay schema, cần sao lưu hoặc điều chỉnh cấu hình không?
- Lỗi còn mở có chặn mục tiêu sử dụng của bản này không?

Mô tả PR phải ghi đúng bằng chứng. Build thành công chỉ chứng minh biên dịch được; test đạt chỉ chứng minh các assertion đã thực thi. PM cần yêu cầu kết quả thủ công cho giao diện, luồng in và các tình huống chưa có kiểm tra tự động.

## Luồng thu phí được giải thích như thế nào?

Ví dụ một căn 48 m² có phí dịch vụ 10.000 đồng/m² và quản lý 7.000 đồng/m². Khi cả hai phí đang áp dụng và có giá hợp lệ, kỳ thu tạo hai khoản: 480.000 đồng và 336.000 đồng, tổng 816.000 đồng.

Kế toán nhận 200.000 đồng cho khoản dịch vụ thì khoản đó còn 280.000 đồng, tổng nợ hộ còn 616.000 đồng. Khi nhận tiếp 300.000 đồng cho khoản dịch vụ, hệ thống cấn 280.000 đồng và lưu dư 20.000 đồng. Khoản quản lý vẫn nợ 336.000 đồng; số dư được xét cấn khi phát hành kỳ sau, không tự phân bổ sang khoản khác trong cùng kỳ.

Các con số trên cần đối chiếu cả bảng kê, lịch sử thu và công nợ. Tổng tiền thực nhận có thể lớn hơn phần đã cấn vào các khoản phải thu.

## Có thể kết luận bản hiện tại đạt toàn bộ chưa?

Các điểm cần quyết định trước khi nghiệm thu:

| Điểm | Ý nghĩa với PM |
|---|---|
| R-02: gửi lại thao tác thu có thể tạo thêm phiếu | Cần cơ chế chống ghi trùng và kiểm tra đồng thời trước khi tin cậy việc thu tiền thực tế. |
| R-03: chọn giá theo ngày phát hành | Yêu cầu chọn giá đầu kỳ chưa đạt; cần sửa code và expected tương ứng. |
| R-04: mã căn/tên hộ lịch sử lấy từ hồ sơ hiện tại | Cần chốt thông tin nào phải giữ nguyên trên chứng từ và bổ sung snapshot. |
| R-05: tên liên hệ có thể lệch tên chủ hộ | Cần thống nhất ý nghĩa trường và nguồn dữ liệu. |
| R-06: nhiều truy vấn chạy trên luồng UI | Cần kiểm tra độ trễ, thao tác lặp và khả năng phản hồi. |

Nếu quyết định giữ lại một điểm cho lần sau, ghi rõ phạm vi được chấp nhận, ảnh hưởng, người phụ trách và điều kiện kiểm tra lại. Không đổi expected theo lỗi hiện có để cho qua.

## Khi được hỏi về bằng chứng đóng góp

Dùng diff, commit, PR, nhận xét review và báo cáo kiểm tra thực tế. Mô tả phần đóng góp theo thay đổi cụ thể và bằng chứng review tương ứng. Các giải thích về thiết kế nên chỉ tới lớp xử lý tương ứng trong [tài liệu dev](DEV.md).
