# Hướng dẫn nghiệp vụ BlueMoon

Tài liệu mô tả cách ban quản trị và kế toán quản lý khoản thu định kỳ trên BlueMoon.

## Vai trò và quyền hạn

| Vai trò | Quyền sử dụng |
|---|---|
| Ban quản trị | Quản lý tài khoản, căn hộ, nhân khẩu, yêu cầu cư dân, khoản thu, đơn giá và phát hành kỳ thu; xem bảng kê và báo cáo tổng quan. |
| Kế toán | Xem hồ sơ căn hộ và nhân khẩu, bảng kê; ghi nhận thanh toán và tra cứu lịch sử thu. |

Các quyền được kiểm tra trong xử lý nghiệp vụ, không chỉ dựa vào việc ẩn nút trên màn hình.

## Thông tin quản lý

| Nhóm thông tin | Trường chính | Quy tắc |
|---|---|---|
| Căn hộ | Mã căn, tầng, mã hộ khẩu, diện tích, chủ hộ, số điện thoại, ngày nhận nhà, tình trạng | Mã căn và mã hộ khẩu là duy nhất; tầng từ 1 đến 30; diện tích lớn hơn 0 và có tối đa hai chữ số thập phân; ngày nhận nhà không ở tương lai. |
| Nhân khẩu | Họ tên, ngày sinh, giới tính, CCCD/CMND, quan hệ, ngày chuyển đến | Họ tên dài 2 đến 50 ký tự chữ; người từ 14 tuổi cần CCCD/CMND 9 hoặc 12 chữ số; mỗi hộ có một chủ hộ. |
| Phương tiện | Căn hộ, loại xe, biển số, trạng thái đăng ký | Mỗi biển số chỉ được đăng ký một lần tại một thời điểm; xe đã ngừng đăng ký được giữ trong lịch sử và không tính cho kỳ thu phát hành sau đó. |
| Hóa đơn tiện ích | Căn hộ, kỳ, loại dịch vụ, số tiền, nhà cung cấp, mã đối chiếu | Mỗi căn hộ có tối đa một hóa đơn cho từng dịch vụ trong một kỳ; được cập nhật trước khi phát hành và được giữ trong lịch sử sau khi đã đưa vào bảng kê. |
| Yêu cầu cư dân | Căn hộ, người báo, nhóm yêu cầu, tiêu đề, mô tả, trạng thái, ghi chú xử lý | Ban quản trị tiếp nhận yêu cầu mới, cập nhật trạng thái và ghi chú xử lý; yêu cầu được giữ lại để tra cứu. |
| Khoản thu | Mã, tên, phân loại, cách tính, đơn giá, ngày hiệu lực, trạng thái | Mã không trùng; phí bắt buộc tính theo diện tích, nhân khẩu, số xe máy, số ô tô hoặc căn hộ; khoản đóng góp tự nguyện không có đơn giá bắt buộc. Đơn giá mới được lưu thành bản ghi riêng; khoản đã phát sinh chỉ được ngừng áp dụng, không xóa khỏi dữ liệu. |
| Kỳ thu | Tháng, ngày phát hành, hạn nộp | Mỗi tháng chỉ phát hành một lần; hạn nộp không trước ngày phát hành. |
| Bảng kê | Căn hộ, khoản thu, số lượng tính phí, đơn giá, số tiền, số đã thanh toán | Thông tin tính phí được lưu tại thời điểm phát hành và không đổi theo các lần cập nhật sau. |
| Công nợ hộ gia đình | Căn hộ, tầng, diện tích, nhân khẩu, phải thu, đã thanh toán, còn nợ, số dư nộp trước, trạng thái, lần thanh toán cuối | Tổng hợp theo hộ trong kỳ; lọc theo trạng thái và khoảng tầng; số đã thanh toán gồm các khoản đã cấn trừ vào kỳ đó, số dư nộp trước được hiển thị riêng. |
| Thông báo thu phí | Kỳ thu, căn hộ, thông tin hộ, chi tiết khoản thu, số dư, hạn nộp | Nội dung phải khớp với bảng kê đã phát hành; có thể xem trước, in riêng hoặc in toàn kỳ. |
| Giao dịch | Ngày thu, người nộp, số tiền nhận, số đã cấn trừ, số dư chuyển kỳ, hình thức, mã tham chiếu, ghi chú, người ghi nhận | Số tiền là số nguyên VND dương; mỗi phiếu thu được lưu riêng trong lịch sử. |
| Tệp CSV | Bảng kê phải thu, lịch sử giao dịch | Xuất các dòng đang hiển thị sau khi lọc; dữ liệu dùng UTF-8 và tiền giữ dạng số để đối soát. |
| Đóng góp | Căn hộ, khoản đóng góp, ngày nhận, số tiền thực nhận, hình thức, mã tham chiếu | Ghi nhận riêng từng lần đóng góp; khoản này không tạo công nợ và không được cộng vào phí bắt buộc. |
| Tài khoản | Tên đăng nhập, tên hiển thị, vai trò, mật khẩu | Ban quản trị tạo tài khoản; mật khẩu được lưu dưới dạng băm và người dùng có thể tự đổi mật khẩu. |

## Quy tắc tính phí và thanh toán

1. Phí theo diện tích được tính bằng `diện tích căn hộ × đơn giá có hiệu lực` tại ngày đầu kỳ; phí theo nhân khẩu dùng số người trong hộ; phí gửi xe dùng số xe máy hoặc ô tô đang đăng ký; phí cố định lấy số tiền từ đơn giá.
2. Đơn giá mặc định gửi xe là 70.000 đồng/xe máy/tháng và 1.200.000 đồng/ô tô/tháng; ban quản trị có thể cập nhật đơn giá theo ngày hiệu lực.
3. Hóa đơn điện, nước và internet được nhập theo số tiền ghi trên chứng từ của nhà cung cấp cho từng căn hộ và kỳ thu.
4. Kết quả phí định kỳ được làm tròn đến đơn vị VND gần nhất.
5. Khi phát hành kỳ thu, hệ thống tạo tối đa một khoản cho mỗi căn hộ và khoản thu định kỳ; mỗi hóa đơn tiện ích đã nhập được tạo thành một khoản riêng.
6. Khoản đã phát hành giữ nguyên diện tích, nhân khẩu, số phương tiện, đơn giá và số tiền đã tính, kể cả khi hồ sơ hoặc đơn giá thay đổi sau này.
7. Có thể ghi nhận nhiều lần thanh toán cho cùng một khoản. Nếu số tiền nhận vượt phần còn nợ, phần đã đủ được cấn vào khoản thu và phần dư được lưu thành số dư của hộ.
8. Trạng thái được cập nhật theo số đã cấn trừ: chưa thu, thu một phần hoặc đã thanh toán. Số dư nộp trước tự động cấn vào các khoản phải thu của hộ khi phát hành kỳ tiếp theo.
9. Giao dịch đã ghi nhận được giữ trong lịch sử để đối soát.
10. Khoản đóng góp tự nguyện chỉ ghi nhận số tiền đã thực nhận, không phát hành khoản phải thu hoặc làm tăng công nợ.

## Quy trình sử dụng

1. Đăng nhập bằng tài khoản thuộc vai trò phù hợp.
2. Ban quản trị tạo tài khoản cho người sử dụng và chọn vai trò phù hợp.
3. Ban quản trị thêm hồ sơ căn hộ, mã hộ khẩu, diện tích, ngày nhận nhà và tình trạng.
4. Thêm nhân khẩu, xác định một chủ hộ, cập nhật ngày sinh, giấy tờ và quan hệ với chủ hộ.
5. Tìm hồ sơ theo mã căn, tầng, họ tên có dấu hoặc không dấu, CCCD/CMND và số điện thoại.
6. Đăng ký phương tiện theo căn hộ trước khi phát hành kỳ thu có phí gửi xe.
7. Nhập hóa đơn điện, nước, internet cho từng căn hộ và kỳ trước khi phát hành.
8. Ghi nhận yêu cầu cư dân theo căn hộ, phân loại nội dung, cập nhật trạng thái và ghi chú xử lý.
9. Tạo khoản thu bắt buộc theo diện tích, nhân khẩu, phương tiện hoặc căn hộ; khai báo đơn giá và ngày bắt đầu áp dụng.
10. Chọn tháng, ngày phát hành và hạn nộp để phát hành kỳ thu.
11. Mở bảng kê để xem công thức, số phải thu, số đã thu và công nợ theo từng căn hộ.
12. Kế toán ghi nhận người nộp, ngày thu, số tiền nhận, phương thức thanh toán và thông tin đối chiếu nếu có.
13. Ghi nhận riêng khoản đóng góp tự nguyện theo số tiền thực nhận.
14. Xem trước hoặc in thông báo thu phí theo căn hộ hay toàn kỳ.
15. Xuất bảng kê hoặc lịch sử thu thành tệp CSV để đối soát.
16. Theo dõi công nợ theo kỳ, lọc trạng thái và khoảng tầng để tìm các hộ còn số dư.
17. Xem tổng quan hoặc lịch sử thu để theo dõi kết quả.

## Tiêu chí chấp nhận

### Hóa đơn tiện ích

- **Khi bắt đầu:** Ban quản trị đã đăng nhập và hóa đơn thuộc một căn hộ đang hoạt động, kỳ chưa phát hành.
- **Khi thực hiện:** Ban quản trị nhập hoặc cập nhật tiền điện, nước hoặc internet cùng nhà cung cấp và mã đối chiếu.
- **Kết quả:** Hóa đơn được đưa vào bảng kê của đúng căn hộ và kỳ; sau khi phát hành, dữ liệu gốc và khoản phải thu không bị thay đổi.

### Đăng ký phương tiện

- **Khi bắt đầu:** Ban quản trị đã đăng nhập và căn hộ đang hoạt động.
- **Khi thực hiện:** Ban quản trị đăng ký xe máy hoặc ô tô bằng biển số chưa được dùng; có thể ngừng đăng ký xe đang hoạt động.
- **Kết quả:** Biển số đang hoạt động không bị trùng; số xe được tính theo loại khi phát hành kỳ thu và dữ liệu kỳ cũ không đổi.

### Yêu cầu cư dân

- **Khi bắt đầu:** Ban quản trị đã đăng nhập và căn hộ đang hoạt động.
- **Khi thực hiện:** Ban quản trị ghi nhận người báo, nhóm yêu cầu, tiêu đề và mô tả; sau đó cập nhật trạng thái cùng ghi chú xử lý.
- **Kết quả:** Yêu cầu được lưu theo căn hộ, có thời điểm tiếp nhận và cập nhật gần nhất, đồng thời vẫn có thể tra cứu sau khi hoàn tất.

### Phát hành kỳ thu

- **Khi bắt đầu:** Căn hộ có diện tích và khoản thu có đơn giá hiệu lực.
- **Khi thực hiện:** Ban quản trị phát hành kỳ thu thuộc tháng đã chọn.
- **Kết quả:** Mỗi khoản phải thu được tính đúng, lưu kèm thông tin tại thời điểm phát hành và không bị tạo trùng nếu kỳ đã tồn tại.

### Ghi nhận thanh toán

- **Khi bắt đầu:** Khoản phải thu còn số dư.
- **Khi thực hiện:** Kế toán lưu ngày thu, người nộp, số tiền và phương thức. Khoản nộp dư được tách thành số dư của hộ.
- **Kết quả:** Phiếu thu xuất hiện trong lịch sử với số đã cấn trừ và số dư nộp trước; số dư được tự động cấn vào kỳ tiếp theo.

### Xuất dữ liệu

- **Khi bắt đầu:** Người dùng đã đăng nhập và bảng kê hoặc lịch sử thu có dữ liệu.
- **Khi thực hiện:** Người dùng xuất danh sách đang hiển thị thành tệp CSV.
- **Kết quả:** Tệp giữ đủ cột, dòng sau lọc và dấu tiếng Việt; hệ thống hỏi trước khi ghi đè tệp cùng tên.

### Phân quyền

- **Khi bắt đầu:** Người dùng đăng nhập bằng tài khoản thuộc một vai trò.
- **Khi thực hiện:** Người dùng gọi thao tác không thuộc quyền hạn của vai trò đó.
- **Kết quả:** Hệ thống từ chối thao tác và giữ nguyên dữ liệu.

### Đăng nhập và mật khẩu

- **Khi bắt đầu:** Người dùng có tài khoản đang hoạt động.
- **Khi thực hiện:** Người dùng nhập sai mật khẩu liên tiếp 5 lần hoặc đổi mật khẩu từ phiên đăng nhập của mình.
- **Kết quả:** Tài khoản bị khóa đăng nhập trong 5 phút; mật khẩu mới chỉ được lưu khi mật khẩu hiện tại hợp lệ và thông tin xác nhận khớp.
