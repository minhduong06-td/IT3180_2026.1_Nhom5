# Kế hoạch kiểm thử Sprint 1

Tài liệu do QA lập cho Sprint 1, dùng để đối chiếu với Definition of Done trong `Sprint1.md` và nghiệm thu luồng thu phí.

## Phạm vi

Kiểm thử luồng thu phí tháng 10/2026 từ đầu đến cuối trên nền dữ liệu demo:

1. Tạo căn hộ và nhân khẩu, gán chủ hộ.
2. Khai báo khoản thu bắt buộc và đơn giá có ngày hiệu lực.
3. Phát hành kỳ thu 10/2026, kiểm tra bảng kê từng căn.
4. Xem trước và in thông báo thu phí.
5. Ghi nhận thanh toán, kiểm tra công nợ và số dư nộp trước.
6. Kiểm tra phân quyền giữa ban quản trị và kế toán.

## Ngoài phạm vi

Phương tiện, hóa đơn điện nước, yêu cầu cư dân, xuất CSV và báo cáo thuộc các story sau Sprint 1. Các phần này chỉ kiểm tra hồi quy khi có thay đổi liên quan.

Lưu ý: source hiện tại đã có một số module mở rộng nói trên. Có code không đồng nghĩa với việc module đó thuộc acceptance scope của Sprint 1. Khi chạy nghiệm thu Sprint 1, chỉ dùng luồng dọc căn hộ–nhân khẩu–khoản thu–kỳ thu–thông báo–thanh toán–công nợ; các case `CONTRIB`, `VEH`, `UTIL`, `REQ`, `EXP` và `E2E-02` là hồi quy/backlog.

## Môi trường

| Thành phần | Giá trị |
|---|---|
| Hệ điều hành | Windows 11 |
| Java | 25 |
| MySQL | 8.4 qua `docker compose up -d` |
| Dữ liệu | Cơ sở dữ liệu sạch, chạy `DatabaseBootstrap` ở lần khởi động đầu |

## Tiêu chí đạt

| Mã | Nội dung | Kết quả mong đợi |
|---|---|---|
| TC-01 | Đăng nhập sai quá số lần cho phép | Tài khoản bị khóa tạm thời, thông báo rõ thời gian mở lại |
| TC-02 | Tạo căn hộ trùng mã căn | Bị từ chối, dữ liệu cũ không đổi |
| TC-03 | Tạo hai chủ hộ trong cùng căn | Bị từ chối, ràng buộc chủ hộ giữ đúng |
| TC-04 | Tạo kỳ thu trùng tháng | Bị từ chối, kỳ đã phát hành giữ nguyên |
| TC-05 | Phát hành kỳ thu 10/2026 | Sinh đúng số tiền hai khoản Sprint 1 theo diện tích căn hộ; không tự đưa feature Sprint 2 vào acceptance |
| TC-06 | Sửa đơn giá sau khi phát hành | Bảng kê đã phát hành không đổi |
| TC-07 | Ghi nhận thanh toán nhỏ hơn số phải thu | Công nợ còn lại đúng, lịch sử thu ghi một phiếu |
| TC-08 | Ghi nhận thanh toán lớn hơn số phải thu | Phần dư chuyển thành số dư nộp trước của hộ |
| TC-09 | Kế toán mở màn hình quản lý tài khoản | Bị chặn ở lớp nghiệp vụ, không chỉ ẩn nút |
| TC-10 | In thông báo thu phí cả kỳ | Nội dung khớp bảng kê, giao diện không treo |

## Cách ghi nhận lỗi

Mỗi lỗi ghi theo mẫu: mã test case, bước tái hiện, kết quả thực tế, kết quả mong đợi, mức độ. Lỗi chặn demo phải được dev xác nhận trước ngày chạy demo nội bộ.

## Ma trận kiểm thử mở rộng và liên kết dữ liệu

Mọi case dưới đây chạy trên cơ sở dữ liệu sạch. Các case có tiền tố **E2E** dùng cùng tập dữ liệu Sprint 1: căn A101 (48,00 m²), A102 (52,50 m²), A103 (65,00 m²); hai khoản bắt buộc theo m²; kỳ 10/2026. Không được đánh dấu pass nếu dữ liệu sinh ở bước trước không xuất hiện đúng ở bước sau.

| Mã | Module | Tiền điều kiện / liên kết | Kiểm thử | Kết quả mong đợi |
|---|---|---|---|---|
| AUTH-01 | Đăng nhập | Tài khoản admin hoạt động | Đăng nhập đúng | Vào đúng shell và menu theo role |
| AUTH-02 | Đăng nhập | Tài khoản admin hoạt động | Nhập sai mật khẩu | Từ chối, không tạo session |
| AUTH-03 | Đăng nhập | Tài khoản thử nghiệm | Sai liên tiếp đến ngưỡng khóa | Bị khóa tạm thời; đăng nhập đúng vẫn bị chặn trong thời gian khóa |
| AUTH-04 | Mật khẩu | Người dùng đã đăng nhập | Đổi mật khẩu sai mật khẩu cũ | Không đổi hash; thông báo rõ |
| AUTH-05 | Mật khẩu | Người dùng đã đăng nhập | Đổi mật khẩu đúng, đăng xuất/đăng nhập lại | Mật khẩu mới dùng được, mật khẩu cũ bị từ chối |
| AUTH-06 | Phân quyền | Tài khoản accountant | Mở quản lý tài khoản | Bị chặn tại service/business layer, không chỉ ẩn menu |
| APT-01 | Căn hộ | Database sạch | Tạo A101 với diện tích hợp lệ | Lưu được, mã căn duy nhất |
| APT-02 | Căn hộ | A101 đã tồn tại | Tạo lại mã A101 | Bị từ chối, bản ghi cũ nguyên vẹn |
| APT-03 | Căn hộ | A101 đã tồn tại | Sửa diện tích trước khi phát hành kỳ | Dữ liệu mới được dùng cho kỳ chưa phát hành |
| RES-01 | Nhân khẩu | A101 tồn tại | Thêm cư dân và đánh dấu chủ hộ | Cư dân gắn đúng A101 |
| RES-02 | Nhân khẩu | A101 đã có chủ hộ | Thêm chủ hộ thứ hai | Bị từ chối; chỉ một chủ hộ |
| RES-03 | Tra cứu | Có A101–A103 và cư dân | Tìm theo căn, tên, giấy tờ, điện thoại | Chỉ trả đúng bản ghi phù hợp |
| FEE-01 | Khoản thu | Database sạch | Tạo phí dịch vụ bắt buộc `PER_M2` | Lưu được basis, trạng thái bắt buộc và đơn giá |
| FEE-02 | Đơn giá | Fee type tồn tại | Thêm đơn giá có ngày hiệu lực | Không nhận giá âm hoặc ngày hiệu lực thiếu |
| FEE-03 | Snapshot | Kỳ 10/2026 đã phát hành | Sửa đơn giá mới | Bảng kê 10/2026 không đổi; kỳ mới dùng giá mới |
| BILL-01 | Kỳ thu | A101–A103 và phí đã cấu hình | Phát hành 10/2026 | Sinh charge theo diện tích × đơn giá cho từng căn |
| BILL-02 | Chống trùng | Kỳ 10/2026 đã phát hành | Phát hành lại cùng tháng | Bị từ chối hoặc không sinh charge trùng |
| BILL-03 | Chi tiết | BILL-01 pass | Mở bảng kê A101 | Có khoản, quantity/diện tích, đơn giá snapshot, tổng, hạn nộp |
| BILL-04 | Thông báo | BILL-01 pass | In/xem trước thông báo A101 và cả kỳ | Nội dung và tổng khớp bảng kê, giao diện không treo |
| PAY-01 | Thanh toán | A101 còn nợ | Thu một phần bằng tiền mặt | `PARTIALLY_PAID`, dư nợ và lịch sử đúng |
| PAY-02 | Thanh toán | PAY-01 pass | Thu phần còn lại bằng chuyển khoản | `PAID`, dư nợ bằng 0, có hai giao dịch |
| PAY-03 | Nộp dư | A102 còn nợ | Thu lớn hơn phải thu | Charge được tất toán, phần dư thành credit của hộ |
| PAY-04 | An toàn thu | Một giao dịch đã lưu | Gửi lại thao tác/cùng tham chiếu | Không tạo giao dịch thu trùng |
| DEBT-01 | Công nợ | PAY-01, PAY-02, PAY-03 pass | Lọc theo kỳ và trạng thái | Chỉ hiển thị căn còn nợ; số phải thu/đã thu/còn nợ nhất quán |
| DEBT-02 | Lịch sử | PAY-02 pass | Mở lịch sử A101 | Thứ tự, người thu, ngày, phương thức và số tiền khớp transaction |
| CONTRIB-01 | Đóng góp | A101 tồn tại | Ghi đóng góp tự nguyện bằng 0 và số dương | Không tạo bắt buộc/không tăng debt; tổng quỹ chỉ cộng số đã ghi nhận |
| VEH-01 | Phương tiện | A101 tồn tại | Đăng ký xe máy và ô tô, biển số khác nhau | Lưu đúng căn, loại, biển số, trạng thái hoạt động |
| VEH-02 | Phương tiện | Một biển số đã tồn tại | Đăng ký trùng biển số | Bị từ chối |
| VEH-03 | Phí xe | Xe hoạt động và xe ngừng hoạt động | Phát hành kỳ mới | Chỉ xe hoạt động trong kỳ được tính; phân biệt đúng đơn giá xe máy/ô tô |
| UTIL-01 | Tiện ích | A101 tồn tại | Nhập điện, nước, internet cùng căn/kỳ | Lưu được theo loại dịch vụ, căn và kỳ |
| UTIL-02 | Tiện ích | Hóa đơn utility đã phát hành vào kỳ | Sửa/xóa hóa đơn đã issued | Bị chặn để giữ snapshot |
| UTIL-03 | Tích hợp billing | UTIL-01 pass | Phát hành kỳ | Utility charge xuất hiện đúng trong bảng kê A101 |
| REQ-01 | Phản ánh | A101 tồn tại | Tạo phản ánh thiếu tiêu đề/nội dung | Validation chặn, không tạo request |
| REQ-02 | Phản ánh | Request hợp lệ tồn tại | Cập nhật trạng thái xử lý | Lịch sử/trạng thái mới được lưu và tìm thấy |
| EXP-01 | CSV | Có charge, payment, utility, contribution | Xuất từng danh sách | UTF-8, header rõ, số liệu khớp bảng đang lọc |
| DASH-01 | Dashboard | Có dữ liệu từ E2E | So sánh KPI với charge/payment/debt | Tổng phải thu, đã thu, còn nợ không lệch nguồn dữ liệu |
| E2E-01 | Luồng Sprint 1 | Database sạch; fixture Sprint 1 chỉ có hai khoản bắt buộc theo m² | Thực hiện APT-01 → FEE-01 → BILL-01 → PAY-01 → DEBT-01 | Toàn bộ dữ liệu liên kết đúng từ căn hộ đến công nợ |
| E2E-02 | Luồng mở rộng | E2E-01 pass | Thêm vehicle/utility rồi phát hành kỳ sau | Không làm thay đổi kỳ 10/2026 đã phát hành; kỳ mới bao gồm đúng dữ liệu mở rộng |

## Bằng chứng tối thiểu cho mỗi lần chạy

- Ảnh màn hình hoặc log cho mỗi case fail/pass quan trọng.
- Mã dữ liệu seed, tháng kỳ thu và tài khoản đã dùng.
- Truy vấn/CSV đối chiếu cho BILL-01, PAY-01/02/03, DEBT-01 và DASH-01.
- Với defect: bước tái hiện, expected/actual, severity, ảnh hưởng regression và case liên quan trong ma trận.
