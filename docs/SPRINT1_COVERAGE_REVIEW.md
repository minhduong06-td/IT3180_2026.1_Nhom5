# Đối chiếu phạm vi Sprint 1

Tài liệu đối chiếu phạm vi Sprint 1 với mã nguồn hiện có, dùng làm căn cứ cho PM quyết định nghiệm thu.

## Phạm vi đối chiếu

Luồng Sprint 1:

`đăng nhập → căn hộ/nhân khẩu → khoản thu/đơn giá → kỳ thu → bảng kê/thông báo → thanh toán → công nợ/lịch sử`.

Nguồn đối chiếu:

- [Hướng dẫn nghiệp vụ](NGHIEP_VU.md), [Kế hoạch kiểm thử](QA_TEST_PLAN.md), [Biên bản kiểm thử](QA_TEST_REPORT.md) và [README](../README.md).
- Các lớp trong [service](../src/main/java/com/bluemoon/service), [UI](../src/main/java/com/bluemoon/ui), [AppRepository](../src/main/java/com/bluemoon/dao/AppRepository.java), [DatabaseBootstrap](../src/main/java/com/bluemoon/config/DatabaseBootstrap.java) và [schema SQL](../src/main/resources/db/schema.sql).
- Các test trong [src/test/java](../src/test/java).

## Coverage trong mã nguồn

“Có luồng” là nhận xét về hiện thực trong source, không đồng nghĩa test đã đạt hoặc đủ điều kiện nghiệm thu.

| Năng lực | Bằng chứng | Trạng thái và phần cần hoàn thiện |
|---|---|---|
| Đăng nhập | `AuthService`, `LoginView`, `account_login_state` | Có BCrypt, ngưỡng 5 lần sai và khóa 5 phút. Cần kiểm tra cả thời điểm hết khóa; thao tác DB vẫn đồng bộ trên UI. |
| Tài khoản/quyền | `AccountService`, `AccountView`, `AccessRules` | Có tạo tài khoản, xem trạng thái và tự đổi mật khẩu; chưa có quản trị khóa/mở tài khoản hoặc đổi role sau tạo. Các test quyền chưa bao phủ toàn bộ service. |
| Căn hộ/hộ | `ApartmentService`, `ApartmentView`, `apartments` | Có tạo/sửa, mã duy nhất, diện tích, ngày và cờ tính phí. Cần thống nhất tên liên hệ/chủ hộ (R-05). |
| Nhân khẩu | `ResidentService`, `ResidentView`, `residents` | Có quan hệ, giấy tờ, tra cứu không dấu và thay chủ hộ có xác nhận; database mới không thêm cư dân mẫu. |
| Loại phí/đơn giá | `FeeService`, `FeeView`, `fee_types`, `fee_rates` | Có lịch sử giá và trạng thái áp dụng; mốc chọn giá chưa đúng yêu cầu đầu kỳ (R-03). |
| Kỳ thu | `BillingService`, `AppRepository.issuePeriod`, `charges` | Có transaction, unique kỳ và snapshot số lượng/giá/tiền; cần hồi quy R-03 và kiểm tra thiếu đơn giá. |
| Bảng kê/thông báo | `ChargesView`, `FeeNoticePrinter` | Có xem bảng kê, in riêng/in cả kỳ. Test tự động chỉ kiểm tra chuỗi chuyển khoản; cần thử bố cục/in thật và giữ đúng thông tin lịch sử (R-04). |
| Thanh toán | `CollectionService`, `payments` | Có thu một phần/nhiều lần, tiền mặt/chuyển khoản và nộp dư; thiếu chống ghi trùng cùng một yêu cầu (R-02). |
| Công nợ/số dư | `DebtView`, `apartment_credits`, `credit_applications` | Có tổng hợp theo kỳ, lọc trạng thái/tầng và cấn số dư khi phát hành kỳ sau; số dư hiển thị là số dư hiện tại. |
| Tra cứu/tổng quan | `ResidentService.search`, `DashboardView` | Có tìm kiếm hồ sơ và KPI kỳ mới nhất theo tháng/năm. Cần đối chiếu tiền cấn trừ, không cộng toàn bộ tiền nhận vào KPI đã thanh toán. |
| Phản hồi giao diện | Các handler `load`, đăng nhập, lưu và phát hành | Bootstrap và gửi trang in có luồng nền; nhiều truy vấn/lưu dữ liệu khác vẫn chạy trên JavaFX Application Thread (R-06). |

## Fixture kiểm thử

Database mới sau bootstrap không có tài khoản hoặc dữ liệu nghiệp vụ; lần mở đầu tiên yêu cầu tạo tài khoản quản trị. Bộ kiểm thử tính phí tự tạo fixture riêng:

- 8 căn A101-A108, tổng diện tích 597,75 m²; 18 nhân khẩu, gồm HEAD/WIFE/CHILD/PARENT/TENANT.
- Tài khoản quản trị và kế toán do test tạo. Không có kỳ thu hoặc thanh toán.
- Chỉ `SERVICE` (10.000 đồng/m²) và `MANAGEMENT` (7.000 đồng/m²) đang áp dụng trong nhóm bắt buộc, hiệu lực từ 01/01/2026.
- Phí vệ sinh và phí xe do test tạo nhưng tắt. Loại tiện ích được tạo khi nhập hóa đơn đầu tiên.
- Một kỳ trên fixture nguyên trạng sinh 16 khoản, tổng 10.161.750 đồng. A101 phải thu 816.000 đồng, A102 phải thu 892.500 đồng.

Fixture này không được nạp vào database ứng dụng. Database cũ được giữ nguyên, không tự thêm các căn hộ, cư dân hoặc khoản thu còn thiếu.

## Các điểm cần xử lý

Các mục dưới đây xác định qua đối chiếu source; trạng thái chạy thực tế ghi riêng trong [Biên bản kiểm thử](QA_TEST_REPORT.md).

| Mã | Ưu tiên | Hiện trạng và hướng xử lý | Kiểm tra lại |
|---|---|---|---|
| R-02 | Cao | `recordPayment()` luôn tạo phiếu mới, `reference` không có unique/check trùng. Thêm mã yêu cầu duy nhất có ràng buộc DB và trả lại kết quả khi gửi lại; chốt quy tắc mã giao dịch ngân hàng. Khóa hàng chỉ giải quyết cạnh tranh cập nhật, không thay cho chống gửi trùng. | PAY-04, PAY-05 |
| R-03 | Cao | Truy vấn giá lấy `issueDate`, khác yêu cầu ngày đầu kỳ. Dùng ngày đầu tháng để chọn giá và cập nhật expected test; không đổi yêu cầu chỉ để khớp test cũ. | RATE-01, RATE-02 |
| R-04 | Trung bình | `listCharges()` và `listPayments()` join mã căn/tên hộ hiện tại. Cần snapshot thông tin nhận diện chứng từ hoặc liên kết hồ sơ hộ có phiên bản; xác định rõ dữ liệu lịch sử và thông tin liên hệ hiện tại. | HIST-01 |
| R-05 | Trung bình | `household_name` vừa là tên liên hệ vừa được dùng như chủ hộ; lưu HEAD có cập nhật sang căn hộ nhưng sửa căn hộ có thể ghi khác. Chốt một nguồn chủ hộ hoặc tách trường liên hệ và sửa nhãn. | RES-04 |
| R-06 | Trung bình | Handler đăng nhập, tải dữ liệu, lưu phiếu và phát hành kỳ gọi JDBC đồng bộ. Chuyển sang `Task`/executor, hiển thị tiến trình, chặn thao tác lặp trong khi chạy và cập nhật UI trên đúng luồng. | UI-01, UI-02 |

R-04/R-05 cần chốt thêm mô hình dữ liệu và cách trình bày; hành vi hiện tại chưa bảo đảm đối soát lịch sử theo thông tin hộ lúc phát hành.

## Phần mở rộng được theo dõi riêng

- Phương tiện và phí gửi xe.
- Hóa đơn điện, nước, internet.
- Yêu cầu/phản ánh cư dân.
- Đóng góp tự nguyện.
- Xuất CSV.
- Lịch sử biến động cư trú.

Các chức năng này có thể kiểm thử hồi quy riêng. Menu hiển thị theo role; source chưa có chế độ tự ẩn các mục ngoài Sprint 1. Trong kịch bản demo, giới thiệu rõ phần mở rộng và không tính các case mở rộng thay cho luồng Sprint 1.

## Điều kiện chốt nghiệm thu

1. Chốt phạm vi Sprint 1 và các yêu cầu còn chưa rõ với PM.
2. Xử lý R-02 và R-03; chạy lại các case liên quan và hồi quy luồng thu phí.
3. Chốt chính sách R-04/R-05, kiểm tra chứng từ cũ sau khi đổi hồ sơ; xử lý hoặc ghi nhận quyết định chấp nhận có giới hạn cho các mục trung bình còn mở.
4. Chạy với JDK 25 và MySQL 8.4 trên database kiểm thử riêng; báo cáo rõ đạt/lỗi/bỏ qua. Test tích hợp bị skip không tính là đã xác minh MySQL.
5. Có bằng chứng thủ công cho UI, phân quyền tại service và in thực tế; lưu log, ảnh/CSV, SQL đối chiếu theo kế hoạch QA.

## Kịch bản demo luồng chính

1. Trên database demo riêng mới, đăng nhập admin; kiểm tra 8 căn, 18 nhân khẩu và đúng hai khoản bắt buộc đang bật.
2. Phát hành kỳ 10/2026, ngày phát hành 01/10/2026, hạn nộp 15/11/2026; kiểm tra 16 khoản và tổng 10.161.750 đồng.
3. Xem bảng kê A101: dịch vụ 480.000 đồng, quản lý 336.000 đồng, tổng 816.000 đồng. Thử in và đối chiếu bản in.
4. Đăng nhập accountant; thu dịch vụ A101 lần đầu 100.000 đồng, lần hai 380.000 đồng; kiểm tra hai phiếu và dịch vụ hết nợ. Phí quản lý vẫn còn nợ 336.000 đồng.
5. Thu dịch vụ A102 số tiền 600.000 đồng; kiểm tra cấn 525.000 đồng và dư 75.000 đồng. Phí quản lý vẫn còn nợ 367.500 đồng.
6. Đăng nhập admin, phát hành kỳ 11/2026; kiểm tra tổng cấn số dư của A102 là 75.000 đồng và số dư còn lại bằng 0.
7. Đối chiếu lịch sử, bảng kê, công nợ và tổng quan theo kỳ đang chọn. Thực hiện các test quyền và case lỗi riêng, không coi demo thuận lợi là đã vượt qua toàn bộ kế hoạch QA.
