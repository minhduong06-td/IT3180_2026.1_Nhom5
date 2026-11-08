# Biên bản kiểm thử

Biên bản ghi kết quả của từng đợt kiểm thử, danh sách lỗi phát hiện và tình trạng xử lý. Mỗi đợt phải ghi rõ người chạy, ngày chạy, bản dựng và môi trường để người khác chạy lại được.

Quy trình và phạm vi nằm trong [kế hoạch kiểm thử](QA_TEST_PLAN.md). Các case chi tiết nằm trong [test case chức năng](QA_TEST_CASE_FUNCTIONAL.md), [test case giao diện](QA_TEST_CASE_UI.md) và [test case tích hợp](QA_TEST_CASE_INTEGRATION.md).

## Đợt 1: kiểm tra chức năng luồng thu phí

| Mục | Nội dung |
|---|---|
| Người chạy | thunganle272 |
| Ngày chạy | 31/10/2026 |
| Bản dựng | nhánh main sau khi merge các nhánh tính phí, hộ khẩu và đóng góp |
| Môi trường | Windows 11, JDK 25.0.5, MySQL 8.4 chạy Docker, schema tạo mới |
| Dữ liệu | bộ A trong [dữ liệu kiểm thử](QA_TEST_DATA.md) |

| Mã case | Kết quả | Ghi chú |
|---|---|---|
| FUN-01 | Đạt | Căn P0508 tạo được, tầng tính đúng là 5 |
| FUN-02 | Đạt | Trùng mã căn bị từ chối |
| FUN-03 | Đạt | Cư dân đầu tiên là chủ hộ, thêm con thành công |
| FUN-04 | Đạt | Chủ hộ thứ hai bị từ chối |
| FUN-05 | Đạt | Khoản thu QA_SERVICE lưu đúng cách tính và đơn giá |
| FUN-06 | Đạt | Đơn giá âm, đơn giá 0 và trùng ngày đều bị chặn |
| FUN-07 | Đạt | 16 khoản, tổng 10.161.750 đồng, khớp bảng chuẩn |
| FUN-08 | Đạt | Thu 100.000 đồng, còn nợ 380.000 đồng |
| FUN-09 | Đạt | Thu tiếp 380.000 đồng, khoản đã đủ, hai phiếu |
| FUN-10 | Đạt | Cấn 525.000 đồng, dư 75.000 đồng |
| FUN-13 | Chưa chạy | Chưa có máy in PDF trên máy chạy |
| FUN-14 | Đạt | Khóa sau 5 lần sai |
| FUN-15 | Đạt | Đổi mật khẩu sai không làm thay đổi dữ liệu |
| FUN-20 | Đạt | Phát hành trùng tháng bị từ chối |
| FUN-21 | Đạt | Bảng kê kỳ 10 giữ nguyên khi thêm đơn giá mới |
| FUN-25 | Đạt | Công nợ khớp bảng kê |

Tổng đợt 1: 15 đạt, 0 không đạt, 1 chưa chạy.

Lỗi phát hiện:

| Mã | Mức | Mô tả | Trạng thái |
|---|---|---|---|
| DEF-01 | Trung bình | Khi chạy Docker trên máy khác, ứng dụng kết nối nhầm cơ sở dữ liệu ngoài localhost làm dữ liệu demo bị lẫn | Đã sửa trong cấu hình kết nối |

## Đợt 2: kiểm tra giao diện

| Mục | Nội dung |
|---|---|
| Người chạy | thunganle272 |
| Ngày chạy | 05/11/2026 |
| Bản dựng | nhánh main sau khi merge các nhánh giao diện và tinh gọn |
| Môi trường | Windows 11, JDK 25.0.5, MySQL 8.4, màn hình 1920x1080 và cửa sổ 1080x700 |
| Dữ liệu | bộ A, có kỳ 10/2026 đã phát hành và đã thu tiền |

| Mã case | Kết quả | Ghi chú |
|---|---|---|
| UI-02 | Đạt | Bảng kê hiển thị đủ cột, số tiền thẳng cột |
| UI-03 | Đạt | Hộp thoại thu tiền lưu xong thì bảng cập nhật ngay |
| UI-04 | Đạt | Bản xem trước khớp bảng kê |
| UI-05 | Đạt | In cả kỳ không treo giao diện |
| UI-06 | Đạt | Đăng nhập vào đúng màn hình theo vai trò |
| UI-07 | Đạt | Sai mật khẩu báo lỗi ngắn, không lộ tài khoản |
| UI-08 | Đạt | Cửa sổ 1080x700 không cắt nút đăng nhập |
| UI-11 | Đạt | Bốn chỉ số tổng quan khớp số liệu |
| UI-14 | Đạt | Danh sách căn hộ hiển thị đủ cột |
| UI-18 | Đạt | Hộp thoại hiện số cấn và số dư trước khi lưu |
| UI-19 | Đạt | Thanh công cụ xuống dòng, không mất nút |
| UI-23 | Đạt | Công nợ lọc đúng, cột số dư tách khỏi đã thanh toán |

Tổng đợt 2: 12 đạt, 0 không đạt.

Lỗi phát hiện:

| Mã | Mức | Mô tả | Trạng thái |
|---|---|---|---|
| DEF-02 | Trung bình | Nhãn phụ đề lặp lại tiêu đề trang, chiếm chỗ trên cửa sổ nhỏ | Đã sửa, kiểm tra lại đạt |
| DEF-03 | Thấp | Bảng rỗng hiển thị thông báo mặc định bằng tiếng Anh | Đã sửa, kiểm tra lại đạt |

## Đợt 3: kiểm tra tích hợp và dữ liệu nâng cấp

| Mục | Nội dung |
|---|---|
| Người chạy | thunganle272 |
| Ngày chạy | 07/11/2026 |
| Bản dựng | nhánh main sau khi merge bản sửa migration thanh toán |
| Môi trường | Windows 11, JDK 25.0.5, MySQL 8.4 container riêng cổng 3308 |
| Dữ liệu | bộ A, bộ D; schema mới và schema cũ có ba căn |

| Mã case | Kết quả | Ghi chú |
|---|---|---|
| INT-01 | Đạt | 16 dòng trong bảng khoản thu, tổng khớp |
| INT-02 | Đạt | Hai phiếu, tổng cấn khớp số phải thu |
| INT-03 | Đạt | Số dư 75.000 đồng cấn đúng vào kỳ 11 |
| INT-04 | Đạt | Số liệu kỳ 10 không đổi |
| INT-05 | Đạt | Ngoại lệ chủ hộ trùng được ném đúng |
| INT-07 | Đạt | Schema cũ giữ ba căn, tạo được quản trị đầu tiên |
| INT-08 | Đạt | Mọi thao tác trái quyền bị chặn ở service |

Tổng đợt 3: 7 đạt, 0 không đạt.

Lỗi phát hiện:

| Mã | Mức | Mô tả | Trạng thái |
|---|---|---|---|
| DEF-04 | Cao | Nâng cấp cơ sở dữ liệu đã có dữ liệu lên bản có số dư nộp trước bị lỗi, ứng dụng không khởi động được | Đã sửa, kiểm tra lại đạt |
| DEF-05 | Cao | Tiền nộp dư phát sinh ở kỳ sau bị cấn ngược vào kỳ đã phát hành trước đó, làm sai số đã thanh toán của kỳ cũ | Đã sửa, kiểm tra lại đạt |

## Đợt 4: chạy lại sau khi sửa lỗi

| Mục | Nội dung |
|---|---|
| Người chạy | thunganle272 |
| Ngày chạy | 08/11/2026 |
| Bản dựng | nhánh main sau khi merge toàn bộ bản sửa |
| Môi trường | như đợt 3, schema tạo mới hoàn toàn |
| Dữ liệu | bộ A, chạy theo kịch bản SC-07 |

| Nhóm | Kết quả |
|---|---|
| Test tự động không cần MySQL | 15 đạt, 0 lỗi, 0 bỏ qua |
| Test tích hợp MySQL | 1 đạt, 0 lỗi, 0 bỏ qua |
| Kịch bản SC-01 đến SC-03 | Đạt, không tái hiện lỗi cũ |
| Kịch bản SC-04 in ấn | Đạt trên máy in PDF |
| Kịch bản SC-05 phân quyền | Đạt |

## Tổng hợp các lỗi

| Mã | Mức | Nội dung | Phát hiện ở | Trạng thái |
|---|---|---|---|---|
| DEF-01 | Trung bình | Kết nối nhầm cơ sở dữ liệu ngoài localhost | Đợt 1 | Đã sửa |
| DEF-02 | Trung bình | Phụ đề lặp tiêu đề, chiếm chỗ trên cửa sổ nhỏ | Đợt 2 | Đã sửa |
| DEF-03 | Thấp | Thông báo bảng rỗng bằng tiếng Anh | Đợt 2 | Đã sửa |
| DEF-04 | Cao | Nâng cấp cơ sở dữ liệu bị lỗi khởi động | Đợt 3 | Đã sửa |
| DEF-05 | Cao | Số dư bị cấn ngược kỳ đã phát hành | Đợt 3 | Đã sửa |

## Vấn đề còn mở

| Mã | Mức | Nội dung | Ảnh hưởng | Hướng xử lý |
|---|---|---|---|---|
| R-02 | Cao | Chưa chặn gửi trùng một yêu cầu thu tiền, mã giao dịch chưa duy nhất | Có thể tạo hai phiếu cho cùng một lần nộp | Thêm mã yêu cầu duy nhất ở cơ sở dữ liệu |
| R-03 | Cao | Chọn đơn giá theo ngày phát hành thay vì ngày đầu kỳ | Sai tiền khi đơn giá đổi giữa tháng | Dùng ngày đầu tháng khi chọn giá |
| R-04 | Trung bình | Chứng từ in lại lấy mã căn và tên hộ theo hồ sơ hiện tại | Bản in cũ có thể khác thời điểm phát hành | Lưu thông tin nhận diện vào chứng từ |
| R-05 | Trung bình | Tên người liên hệ của căn hộ và tên chủ hộ có thể lệch nhau | Nhãn trên báo cáo không thống nhất | Chốt một nguồn tên chủ hộ |
| R-06 | Trung bình | Nhiều thao tác đọc ghi cơ sở dữ liệu chạy trên luồng giao diện | Giao diện có thể đứng khi mạng chậm | Chuyển sang luồng nền, hiện chỉ báo đang xử lý |

## Điều kiện ghi biên bản

Mỗi đợt cần ghi đủ: người chạy, ngày giờ, bản dựng, môi trường, bộ dữ liệu, danh sách case đã chạy, và vị trí lưu bằng chứng.

Bằng chứng gồm ảnh màn hình, tệp PDF bản in, báo cáo Surefire và câu truy vấn đối chiếu. Lỗi luôn ghi kèm bước tái hiện và số liệu thực tế.

Không tính test bị bỏ qua vào số đạt. Không kết luận đạt cho case chưa chạy trên môi trường thật.
