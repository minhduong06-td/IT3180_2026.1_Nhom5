# Ghi chú cho PM

## Sản phẩm và luồng dữ liệu

BlueMoon giúp ban quản trị lập khoản phải thu theo tháng và kế toán ghi nhận tiền cư dân nộp. Luồng dữ liệu: căn hộ và cư dân → khoản thu và đơn giá → kỳ thu → bảng kê → thanh toán → công nợ. Phương tiện, tiện ích và đóng góp đi kèm luồng này.

Phân công: dev làm trên nhánh, QA kiểm tra và đưa kết quả, PM đối chiếu yêu cầu rồi quyết định tích hợp. QA không cần commit để đưa bằng chứng.

## Nhánh, PR và merge commit

Mỗi nhánh gom một thay đổi có mục đích rõ. PR cho người review thấy vấn đề, cách xử lý, phần bị ảnh hưởng và bằng chứng kiểm tra. Merge commit giữ ranh giới giữa các thay đổi để lần lỗi hoặc revert cả thay đổi khi cần.

Lịch sử nhánh và commit không nói được ai đã review. Cùng một tài khoản vừa code vừa merge thì ghi là tự review, đừng ghi thành phê duyệt độc lập của PM hoặc QA.

## Khởi tạo database demo

Bootstrap chỉ tạo và nâng cấp schema; dữ liệu mẫu do Docker nạp từ `database/demo-data.sql` khi tạo volume mới. Tách hai việc này để tránh trường hợp dữ liệu cố định không đủ mã căn mà bước thêm cư dân yêu cầu, làm ứng dụng lỗi ngay lúc khởi động.

Chưa có user thì ứng dụng cho tạo quản trị đầu tiên; có rồi thì vào màn hình đăng nhập. Người clone repo không phải nhập tay dữ liệu nghiệp vụ.

Các số liệu lịch sử trong [biên bản QA](../qa/QA_TEST_REPORT.md) có lượt chạy trên database thử nghiệm riêng và không dùng làm hướng dẫn hiện tại. Quy trình hiện tại chỉ chấp nhận kết quả chạy trên database demo `bluemoon`, được khởi tạo từ `schema.sql` và `database/demo-data.sql`.

Riêng phần khởi tạo này không đóng các lỗi tài chính đang mở.

## Tinh gọn giao diện

Màn hình làm việc phải giúp người dùng tìm hồ sơ, kiểm tra số tiền và thực hiện thao tác. Slogan ở màn hình đăng nhập và phụ đề lặp lại tên trang chiếm chỗ mà không giúp gì cho ba việc đó. Phần giữ lại là hướng dẫn nhập liệu, quy tắc phát hành, cảnh báo và các con số cần đối soát.

Menu cuộn và thanh công cụ xuống dòng để thao tác vẫn tới được ở cửa sổ nhỏ. Khi review phải xem cả ảnh render lẫn dùng thử thật: bỏ chữ chỉ ổn nếu người dùng vẫn hiểu trạng thái và biết bước tiếp theo.

## Checklist trước khi merge PR

- Vấn đề có đối chiếu được với yêu cầu hoặc lỗi cụ thể không?
- Diff có đúng phạm vi nhánh, có thay đổi ngoài yêu cầu không?
- Quyền ở service, transaction và dữ liệu lịch sử có bị ảnh hưởng không?
- Test nào đã chạy trên commit nào, có test bị bỏ qua không?
- Có đổi schema, cần sao lưu hay chỉnh cấu hình không?
- Lỗi còn mở có chặn mục tiêu sử dụng của bản này không?

Mô tả PR phải khớp với bằng chứng. Build xanh chỉ nói lên là biên dịch được, test xanh chỉ nói lên các assertion đã chạy. Giao diện, luồng in và các tình huống không có test tự động thì cần kết quả thao tác tay.

Phần đóng góp của từng người thì căn cứ vào diff, commit, PR, nhận xét review và báo cáo kiểm tra thật. Giải thích thiết kế nên trỏ tới lớp xử lý tương ứng trong [tài liệu dev](DEV.md).

## Ví dụ luồng thu phí

Căn 48 m², phí dịch vụ 10.000 đồng/m² và phí quản lý 7.000 đồng/m². Cả hai đang áp dụng và có giá hợp lệ thì kỳ thu tạo hai khoản: 480.000 và 336.000 đồng, tổng 816.000 đồng.

Kế toán nhận 200.000 đồng cho khoản dịch vụ: khoản đó còn 280.000 đồng, nợ hộ còn 616.000 đồng. Nhận tiếp 300.000 đồng cho khoản dịch vụ: cấn 280.000 đồng, lưu dư 20.000 đồng. Khoản quản lý vẫn nợ 336.000 đồng; số dư chờ tới kỳ sau mới cấn, không tự phân bổ sang khoản khác trong cùng kỳ.

Đối chiếu cả bảng kê, lịch sử thu và công nợ. Tổng tiền thực nhận có thể lớn hơn phần đã cấn vào các khoản phải thu.

## Các điểm chưa chốt nghiệm thu

| Điểm | Ý nghĩa với PM |
|---|---|
| R-02: gửi lại thao tác thu | Mã giao dịch trùng bị từ chối qua repository; đã thử đồng thời trên MySQL. Thử lại phải giữ cùng mã, và vẫn cần kiểm tra các cách ghi ngoài ứng dụng. |
| R-03: chọn giá theo ngày đầu kỳ | Đã kiểm tra trường hợp đổi giá giữa tháng trên MySQL; cần hồi quy lại mỗi khi sửa chức năng tính phí. |
| R-04: mã căn/tên hộ lịch sử lấy từ hồ sơ hiện tại | Cần chốt thông tin nào phải giữ nguyên trên chứng từ rồi bổ sung snapshot. |
| R-05: tên liên hệ có thể lệch tên chủ hộ | Cần thống nhất ý nghĩa trường và nguồn dữ liệu. |
| R-06: nhiều truy vấn chạy trên luồng UI | Cần kiểm tra độ trễ, thao tác lặp và khả năng phản hồi. |

Nếu giữ lại một điểm cho lần sau thì ghi rõ phạm vi chấp nhận, ảnh hưởng, người phụ trách và điều kiện kiểm tra lại. Không sửa expected theo lỗi hiện có để cho qua.
