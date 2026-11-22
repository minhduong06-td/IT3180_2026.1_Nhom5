# Đặc tả yêu cầu BlueMoon

## Mục đích

BlueMoon là ứng dụng desktop giúp ban quản trị và kế toán quản lý căn hộ, cư dân và các khoản thu của chung cư. Tài liệu tóm tắt phạm vi hiện có để nhóm, người dùng và QA cùng nắm.

Quy tắc tính phí chi tiết nằm trong [hướng dẫn nghiệp vụ](../guides/NGHIEP_VU.md). Các điểm còn mở theo dõi trong [đối chiếu Sprint 1](../qa/SPRINT1_COVERAGE_REVIEW.md).

## Người dùng

| Vai trò | Việc chính |
|---|---|
| Ban quản trị (`ADMIN`) | Quản lý hồ sơ, khoản thu, kỳ thu, tiện ích, phương tiện, yêu cầu cư dân, đóng góp và tài khoản. |
| Kế toán (`ACCOUNTANT`) | Tra cứu bảng kê và công nợ, ghi nhận thanh toán, xem lịch sử thu, ghi nhận đóng góp. |
| Cư dân (`RESIDENT`) | Xem thông tin căn hộ và cư dân trong hộ, theo dõi khoản phải thu và lịch sử, gửi yêu cầu cho ban quản trị. |

Quyền được kiểm tra ở tầng xử lý nghiệp vụ. Ẩn một mục trên giao diện không thay được việc kiểm tra quyền.

## Chức năng

### Tài khoản

- Đăng nhập bằng email hoặc số điện thoại đã đăng ký; tạo tài khoản quản trị đầu tiên khi hệ thống chưa có tài khoản.
- Phân quyền theo ba vai trò trên, cho người dùng tự đổi mật khẩu và khóa đăng nhập tạm thời sau nhiều lần nhập sai.
- Cư dân chỉ xem được thông tin thuộc căn hộ của mình.

### Hồ sơ chung cư

- Ban quản trị thêm và cập nhật căn hộ, cư dân, chủ hộ, phương tiện và biến động cư trú.
- Hệ thống kiểm tra mã căn, hộ khẩu, giấy tờ và biển số theo quy tắc trong hướng dẫn nghiệp vụ.
- Biến động cư trú được lưu để tra cứu; hiện chưa làm thay đổi số nhân khẩu dùng để tính phí.

### Khoản thu và kỳ thu

- Ban quản trị tạo khoản thu, bật/tắt khoản thu và đặt đơn giá theo ngày hiệu lực.
- Hệ thống tính được theo diện tích, nhân khẩu, phương tiện, mức cố định hoặc hóa đơn tiện ích.
- Phát hành kỳ tháng thì hệ thống tạo bảng kê cho các căn hộ và khoản thu đủ điều kiện. Mỗi tháng chỉ có một kỳ.
- Bảng kê lưu cách tính, số lượng, đơn giá và số tiền tại lúc phát hành. Sửa hồ sơ sau đó không tính lại kỳ đã phát hành.

### Thanh toán và đối soát

- Kế toán ghi nhận tiền mặt hoặc chuyển khoản cho từng khoản phải thu.
- Tiền nhận cấn vào khoản đang chọn; phần dư giữ lại cho căn hộ và có thể cấn vào kỳ phát hành sau.
- Mã giao dịch dùng để từ chối ghi nhận trùng mã.
- Người dùng tra cứu bảng kê, công nợ, lịch sử thu, in thông báo và xuất CSV.

### Tiện ích, đóng góp và yêu cầu cư dân

- Ban quản trị nhập hóa đơn điện, nước hoặc internet trước khi phát hành kỳ.
- Đóng góp tự nguyện ghi nhận riêng, không cộng vào công nợ bắt buộc.
- Cư dân gửi yêu cầu; ban quản trị phân loại, cập nhật trạng thái và ghi chú xử lý.

## Quy tắc quan trọng

- Đơn giá của kỳ chọn theo ngày đầu tháng: lấy mức giá gần nhất có hiệu lực vào ngày đó.
- Tiền làm tròn đến đồng theo quy tắc `HALF_UP`.
- Phát hành kỳ và ghi nhận thanh toán phải xong cả hoặc không ghi gì.
- Bản hiện tại chưa chụp đầy đủ thông tin căn hộ lên chứng từ cũ; xem điểm R-04 trong hướng dẫn nghiệp vụ.

## Yêu cầu chất lượng

- Ứng dụng desktop JavaFX, dữ liệu trong MySQL, giao diện tiếng Việt.
- Số tiền tính và hiển thị phải nhất quán; thao tác tài chính cần có lịch sử để đối chiếu.
- Mật khẩu băm bằng BCrypt, không lưu mật khẩu thuần.
- Lỗi kết nối hoặc dữ liệu không hợp lệ phải báo cho người dùng; thao tác ghi không để lại dữ liệu dở dang.

## Điều kiện nghiệm thu chính

- Mỗi vai trò chỉ làm được thao tác được phép; tầng xử lý cũng phải từ chối khi sai quyền.
- Phát hành kỳ tạo đúng khoản và số tiền theo hồ sơ, đơn giá và ngày đầu kỳ; lỗi giữa chừng không để lại dữ liệu một phần.
- Thanh toán cập nhật đúng số đã cấn, số còn nợ, số dư và lịch sử; mã giao dịch trùng bị từ chối.
- Bảng kê, công nợ, lịch sử thu, thông báo in và CSV khớp dữ liệu đã lưu.
- Kiểm tra theo [kế hoạch QA](../qa/QA_TEST_PLAN.md) và [test case chức năng](../qa/QA_TEST_CASE_FUNCTIONAL.md).

## Những việc bản hiện tại chưa làm

- Không gửi mã xác minh qua email hay SMS khi đặt lại mật khẩu.
- Biến động cư trú chưa dùng trong phép tính số nhân khẩu.
- Quản trị viên chưa có thao tác chủ động khóa tài khoản hay đổi vai trò sau khi tạo.
- Đây là bản desktop phục vụ đồ án và demo; cấu hình mẫu trong repo không dành cho dữ liệu thật.
