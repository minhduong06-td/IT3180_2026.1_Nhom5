# Hướng dẫn nghiệp vụ BlueMoon

Tài liệu mô tả cách ban quản trị và kế toán quản lý khoản thu định kỳ trên BlueMoon.

## Vai trò và quyền hạn

| Vai trò | Quyền sử dụng |
|---|---|
| Ban quản trị | Quản lý tài khoản, căn hộ, nhân khẩu, khoản thu, đơn giá và phát hành kỳ thu; xem bảng kê và báo cáo tổng quan. |
| Kế toán | Xem hồ sơ căn hộ và nhân khẩu, bảng kê; ghi nhận thanh toán và tra cứu lịch sử thu. |

Các quyền được kiểm tra trong xử lý nghiệp vụ, không chỉ dựa vào việc ẩn nút trên màn hình.

## Thông tin quản lý

| Nhóm thông tin | Trường chính | Quy tắc |
|---|---|---|
| Căn hộ | Mã căn, tầng, mã hộ khẩu, diện tích, chủ hộ, số điện thoại, ngày nhận nhà, tình trạng | Mã căn và mã hộ khẩu là duy nhất; tầng từ 1 đến 30; diện tích lớn hơn 0 và có tối đa hai chữ số thập phân; ngày nhận nhà không ở tương lai. |
| Nhân khẩu | Họ tên, ngày sinh, giới tính, CCCD/CMND, quan hệ, ngày chuyển đến | Họ tên dài 2 đến 50 ký tự chữ; người từ 14 tuổi cần CCCD/CMND 9 hoặc 12 chữ số; mỗi hộ có một chủ hộ. |
| Khoản thu | Mã, tên, cách tính, đơn giá, ngày hiệu lực | Mã không trùng; đơn giá lớn hơn 0; đơn giá mới được lưu thành bản ghi riêng. |
| Kỳ thu | Tháng, ngày phát hành, hạn nộp | Mỗi tháng chỉ phát hành một lần; hạn nộp không trước ngày phát hành. |
| Bảng kê | Căn hộ, khoản thu, diện tích, đơn giá, số tiền, số đã thu | Thông tin tính phí được lưu tại thời điểm phát hành và không đổi theo các lần cập nhật sau. |
| Giao dịch | Thời gian, số tiền, hình thức, mã tham chiếu, ghi chú, người ghi nhận | Số tiền phải là số nguyên VND dương; từng lần thu được lưu riêng trong lịch sử. |
| Tài khoản | Tên đăng nhập, tên hiển thị, vai trò, mật khẩu | Ban quản trị tạo tài khoản; mật khẩu được lưu dưới dạng băm và người dùng có thể tự đổi mật khẩu. |

## Quy tắc tính phí và thanh toán

1. Phí theo diện tích được tính bằng `diện tích căn hộ × đơn giá có hiệu lực` tại ngày đầu kỳ. Khoản thu cố định lấy số tiền từ đơn giá.
2. Kết quả được làm tròn đến đơn vị VND gần nhất.
3. Khi phát hành kỳ thu, hệ thống tạo tối đa một khoản cho mỗi căn hộ và khoản thu trong kỳ đó.
4. Khoản đã phát hành giữ nguyên diện tích, đơn giá và số tiền đã tính, kể cả khi hồ sơ hoặc đơn giá thay đổi sau này.
5. Có thể ghi nhận nhiều lần thanh toán cho cùng một khoản. Tổng tiền đã thu không được vượt quá số phải thu.
6. Trạng thái được cập nhật theo số dư: chưa thu, thu một phần hoặc đã thanh toán.
7. Giao dịch đã ghi nhận được giữ trong lịch sử để đối soát.

## Quy trình sử dụng

1. Đăng nhập bằng tài khoản thuộc vai trò phù hợp.
2. Ban quản trị tạo tài khoản cho người sử dụng và chọn vai trò phù hợp.
3. Ban quản trị thêm hồ sơ căn hộ, mã hộ khẩu, diện tích, ngày nhận nhà và tình trạng.
4. Thêm nhân khẩu, xác định một chủ hộ, cập nhật ngày sinh, giấy tờ và quan hệ với chủ hộ.
5. Tìm hồ sơ theo mã căn, tầng, họ tên có dấu hoặc không dấu, CCCD/CMND và số điện thoại.
6. Tạo khoản thu bắt buộc theo diện tích và khai báo đơn giá, ngày bắt đầu áp dụng.
7. Chọn tháng, ngày phát hành và hạn nộp để phát hành kỳ thu.
8. Mở bảng kê để xem công thức, số phải thu, số đã thu và công nợ theo từng căn hộ.
9. Kế toán ghi nhận số tiền nhận được, phương thức thanh toán và thông tin đối chiếu nếu có.
10. Xem tổng quan hoặc lịch sử thu để theo dõi kết quả.

## Tiêu chí chấp nhận

### Phát hành kỳ thu

- **Khi bắt đầu:** Căn hộ có diện tích và khoản thu có đơn giá hiệu lực.
- **Khi thực hiện:** Ban quản trị phát hành kỳ thu thuộc tháng đã chọn.
- **Kết quả:** Mỗi khoản phải thu được tính đúng, lưu kèm thông tin tại thời điểm phát hành và không bị tạo trùng nếu kỳ đã tồn tại.

### Ghi nhận thanh toán

- **Khi bắt đầu:** Khoản phải thu còn số dư.
- **Khi thực hiện:** Kế toán ghi nhận số tiền dương không vượt số dư.
- **Kết quả:** Giao dịch xuất hiện trong lịch sử, số đã thu và số còn nợ được cập nhật.

### Phân quyền

- **Khi bắt đầu:** Người dùng đăng nhập bằng tài khoản thuộc một vai trò.
- **Khi thực hiện:** Người dùng gọi thao tác không thuộc quyền hạn của vai trò đó.
- **Kết quả:** Hệ thống từ chối thao tác và giữ nguyên dữ liệu.

### Đăng nhập và mật khẩu

- **Khi bắt đầu:** Người dùng có tài khoản đang hoạt động.
- **Khi thực hiện:** Người dùng nhập sai mật khẩu liên tiếp 5 lần hoặc đổi mật khẩu từ phiên đăng nhập của mình.
- **Kết quả:** Tài khoản bị khóa đăng nhập trong 5 phút; mật khẩu mới chỉ được lưu khi mật khẩu hiện tại hợp lệ và thông tin xác nhận khớp.
