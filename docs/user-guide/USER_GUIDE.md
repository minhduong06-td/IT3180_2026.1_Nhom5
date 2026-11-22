# Hướng dẫn sử dụng BlueMoon

## Bắt đầu

1. Mở ứng dụng và đăng nhập bằng email hoặc số điện thoại đã đăng ký.
2. Database chưa có tài khoản thì tạo tài khoản quản trị theo hướng dẫn trên màn hình.
3. Các mục ở thanh bên thay đổi theo vai trò. Xong việc thì **Đăng xuất**; muốn đổi mật khẩu thì chọn **Đổi mật khẩu**.

Ứng dụng mẫu có sẵn tài khoản trong `database/demo-data.sql`, mật khẩu ban đầu ghi trong README gốc. Chỉ dùng với dữ liệu demo, và đổi mật khẩu nếu tiếp tục dùng.

## Ban quản trị

### Chuẩn bị hồ sơ

1. Mở **Căn hộ**, thêm căn hộ và thông tin liên hệ.
2. Mở **Cư dân**, thêm cư dân vào đúng căn hộ rồi chọn chủ hộ.
3. Mở **Phương tiện** để đăng ký xe. Xe không còn thuộc căn hộ thì ngừng đăng ký.

Dữ liệu bị từ chối thì sửa theo nội dung báo lỗi rồi lưu lại.

### Thiết lập và phát hành kỳ thu

1. Vào **Khoản thu**, tạo khoản thu và chọn cách tính.
2. Thêm đơn giá kèm ngày bắt đầu áp dụng. Phí theo hóa đơn thì nhập hóa đơn ở **Điện, nước, internet** trước khi phát hành.
3. Mở **Kỳ thu**, chọn tháng, ngày phát hành và hạn nộp rồi phát hành.
4. Mở bảng kê của kỳ vừa tạo, kiểm tra khoản thu và số tiền trước khi in thông báo hoặc thu tiền.

Mỗi tháng chỉ phát hành được một kỳ, nên kiểm tra hồ sơ, đơn giá và hóa đơn trước bước này.

### Các việc khác

- **Yêu cầu cư dân:** xem yêu cầu mới, phân loại, cập nhật trạng thái và ghi chú xử lý.
- **Biến động cư trú:** lưu trạng thái, thời gian và lý do để tra cứu.
- **Đóng góp:** tạo loại đóng góp rồi ghi số tiền thực nhận. Khoản này theo dõi riêng, không nhập vào công nợ.
- **Tài khoản:** tạo tài khoản theo vai trò. Người dùng tự đổi mật khẩu sau khi đăng nhập.

## Kế toán

1. Mở **Bảng kê phải thu** hoặc **Công nợ** để tìm căn hộ và khoản còn phải thu.
2. Chọn khoản cần thu, nhập người nộp, ngày thu, số tiền, hình thức thanh toán và mã đối chiếu.
3. Kiểm tra lại rồi lưu. Tiền nhận nhiều hơn số còn nợ thì phần chênh được giữ làm số dư của căn hộ.
4. Mở **Lịch sử thu** để đối chiếu phiếu đã ghi. Màn hình hỗ trợ in thông báo và xuất CSV.

Chuyển khoản thì nên nhập mã tham chiếu ngân hàng vào ô mã giao dịch. Thử lại một thao tác chưa rõ kết quả thì giữ nguyên mã: mã trùng sẽ bị từ chối, nhờ vậy không ghi nhận hai lần.

## Cư dân

- **Căn hộ của tôi** và **Cư dân trong hộ** hiện thông tin thuộc hộ của tài khoản đang đăng nhập.
- **Bảng kê phải thu**, **Công nợ** và **Lịch sử thu** để theo dõi khoản cần nộp và khoản đã ghi nhận.
- Mở **Yêu cầu cư dân** để gửi nội dung cần hỗ trợ và theo dõi trạng thái.

## In, xuất và xem số liệu

- Lọc đúng kỳ và căn hộ trước khi in thông báo hoặc xuất CSV.
- Xuất xong thì kiểm tra lại tổng tiền và nội dung.
- Đóng góp tự nguyện không làm giảm công nợ bắt buộc. Số dư nộp trước chỉ được xét khi phát hành kỳ sau.

Thao tác báo lỗi thì đọc nội dung trên màn hình, rồi kiểm tra quyền tài khoản, dữ liệu bắt buộc và trạng thái kỳ thu. Quy tắc tính tiền chi tiết nằm trong [hướng dẫn nghiệp vụ](../guides/NGHIEP_VU.md).
