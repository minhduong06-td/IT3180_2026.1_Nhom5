# Biên bản kiểm thử

Biên bản ghi kết quả của từng đợt kiểm thử, danh sách lỗi phát hiện và tình trạng xử lý. Mỗi đợt phải ghi rõ người chạy, ngày chạy, bản dựng và môi trường để người khác chạy lại được.

### Kiểm tra giao diện bổ sung sau Sprint 1

Các bước dưới đây dùng dữ liệu hiện có của dự án. Kết quả chỉ áp dụng cho thao tác và dữ liệu đã quan sát; không thay thế toàn bộ test case, kiểm tra ghi dữ liệu hoặc kiểm tra bảo mật ở lớp service.

| Bước kiểm tra | Kết quả thực tế | Ảnh |
|---|---|---|
| Mở ứng dụng, đăng nhập tài khoản ban quản trị | Màn hình đăng nhập hiển thị; đăng nhập thành công và vào Tổng quan. | [Đăng nhập](images/qa-20261125-01-dang-nhap.png), [Tổng quan quản trị](images/qa-20261125-02-tong-quan.png) |
| Tìm căn hộ A101 | Tìm `A101`, danh sách còn một dòng A101, diện tích 48 m². | [Kết quả tìm kiếm](images/qa-20261125-03-tim-can-ho.png) |
| Xem công nợ kỳ 11/2026 | Hiển thị phải thu 10.905.500 đồng, đã thanh toán 4.512.000 đồng, còn 6.393.500 đồng; ba số khớp phép trừ và tổng đọc từ database. | [Công nợ](images/qa-20261125-04-cong-no.png) |
| Xem bảng kê và thông báo A101 | Hai khoản lần lượt 480.000 và 336.000 đồng; đã thanh toán 436.000, còn 380.000 đồng. Khớp dòng A101 trên màn hình công nợ. Chưa in thực tế. | [Bảng kê](images/qa-20261125-05-bang-ke.png), [Chi tiết A101](images/qa-20261125-06-thong-bao.png) |
| Đăng nhập kế toán và cư dân | Cả hai đăng nhập thành công. Menu kế toán khác menu quản trị; tổng quan cư dân chỉ hiển thị A101 trong dữ liệu đã quan sát. Chưa thử truy cập trái quyền ở service trong lượt UI này. | [Kế toán](images/qa-20261125-07-vai-tro-ke-toan.png), [Cư dân](images/qa-20261125-08-vai-tro-cu-dan.png) |

Điểm cần xem lại: chi tiết bảng kê A101 hiển thị `Nhân khẩu -`, còn màn hình công nợ hiển thị 3 nhân khẩu. Cần xác định đây là cách biểu diễn dữ liệu snapshot hay lỗi hiển thị trước khi kết luận.

### Kiểm tra sau lần chỉnh sửa đầu tiên

- Bảng kê lấy số nhân khẩu từ số lượng được lưu trong khoản thu theo đầu người, không phải số cư dân hiện tại. Nếu kỳ thu không có khoản theo nhân khẩu thì không có snapshot này. Đã sửa nhãn để chỉ hiển thị **nhân khẩu tính phí** khi có số liệu; ảnh ở trên là trước sửa, chưa chụp lại.
- Truy vấn chọn đơn giá đã được đổi sang ngày đầu kỳ; đã bổ sung test quy tắc ngày đầu kỳ và điều chỉnh expected trong test MySQL. Chưa xác minh lại trường hợp đổi giá giữa tháng trên MySQL sau sửa.
- `mvnw.cmd test`: 29 test, 28 đạt, 0 lỗi, 1 bỏ qua; test tích hợp MySQL bị bỏ qua do chưa cấu hình schema kiểm thử. Kết quả 28/28 ở phần trên là lần chạy **trước các thay đổi này**, không phải xác nhận bản hiện tại.
- Tại lần kiểm tra này, R-02 chưa sửa và chưa được kiểm thử lại. Xem kết quả mới hơn bên dưới.

### Hồi quy R-02 và R-03 sau khi chỉnh sửa

Chạy `MySqlBillingFlowTest#existingDatabaseRateAndDuplicatePaymentRegression` với MySQL 8.4.13 trên database hiện có, dùng kỳ 03/2099 và mã riêng để nhận diện dữ liệu kiểm thử. Trước khi sửa R-02, hai yêu cầu đồng thời cùng mã tạo hai phiếu và test thất bại. Sau sửa, chạy lại test: **1 test đạt, 0 lỗi, 0 bỏ qua**; hai yêu cầu đồng thời chỉ tạo một phiếu, lần gửi lại tiếp theo bị từ chối, mã trống bị từ chối.

Trong cùng test, đơn giá SERVICE tăng từ 10.000 lên 12.000 đồng/m² giữa tháng, phát hành ngày 20: A101 48 m² dùng giá đầu kỳ 10.000 đồng/m² và số tiền 480.000 đồng. Đã kiểm tra sau lượt chạy không còn kỳ, đơn giá hoặc phiếu thử; database còn 10 căn, 25 cư dân, 1 kỳ và 9 phiếu như trước.

Bộ `mvnw.cmd test` không cấu hình MySQL sau sửa: 30 test, 28 đạt, 0 lỗi, 2 bỏ qua. Test MySQL mục tiêu ở trên đã chạy riêng với cấu hình database; test MySQL cũ yêu cầu schema rỗng không chạy trên dữ liệu hiện có. Chưa xác nhận toàn bộ luồng thủ công hoặc in PDF trên bản mới.

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
| R-02 | Cao | Đã kiểm tra trùng mã bằng khóa MySQL khi ghi qua repository | Không phát hiện giao dịch trùng nếu dùng hai mã khác nhau hoặc ghi trực tiếp bằng SQL | Giữ cùng mã khi thử lại; tiếp tục hồi quy và chốt quy tắc đối soát ngân hàng |
| R-03 | Cao | Đã chọn giá đầu kỳ và kiểm tra tình huống đổi giá giữa tháng trên MySQL | Không thay đổi các kỳ đã phát hành | Tiếp tục hồi quy khi thay đổi tính phí |
| R-04 | Trung bình | Chứng từ in lại lấy mã căn và tên hộ theo hồ sơ hiện tại | Bản in cũ có thể khác thời điểm phát hành | Lưu thông tin nhận diện vào chứng từ |
| R-05 | Trung bình | Tên người liên hệ của căn hộ và tên chủ hộ có thể lệch nhau | Nhãn trên báo cáo không thống nhất | Chốt một nguồn tên chủ hộ |
| R-06 | Trung bình | Nhiều thao tác đọc ghi cơ sở dữ liệu chạy trên luồng giao diện | Giao diện có thể đứng khi mạng chậm | Chuyển sang luồng nền, hiện chỉ báo đang xử lý |
