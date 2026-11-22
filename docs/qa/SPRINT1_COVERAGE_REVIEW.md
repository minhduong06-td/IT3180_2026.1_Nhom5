# Đối chiếu phạm vi Sprint 1

Tài liệu đối chiếu phạm vi Sprint 1 với mã nguồn hiện có, làm căn cứ để PM quyết định nghiệm thu.

## Phạm vi và nguồn

Luồng Sprint 1: `đăng nhập → căn hộ/nhân khẩu → khoản thu/đơn giá → kỳ thu → bảng kê/thông báo → thanh toán → công nợ/lịch sử`.

Nguồn đối chiếu:

- [Hướng dẫn nghiệp vụ](../guides/NGHIEP_VU.md), [Kế hoạch kiểm thử](QA_TEST_PLAN.md), [Biên bản kiểm thử](QA_TEST_REPORT.md) và [README](../../README.md).
- Các lớp trong [service](../../src/main/java/com/bluemoon/service), [UI](../../src/main/java/com/bluemoon/ui), [AppRepository](../../src/main/java/com/bluemoon/dao/AppRepository.java), [DatabaseBootstrap](../../src/main/java/com/bluemoon/config/DatabaseBootstrap.java) và [schema SQL](../../src/main/resources/db/schema.sql).
- Các test trong [src/test/java](../../src/test/java).

## Những gì đã có trong mã nguồn

"Có luồng" chỉ nói về hiện thực trong source, không nói test đã đạt hay đã đủ điều kiện nghiệm thu.

| Năng lực | Bằng chứng | Trạng thái và phần còn thiếu |
|---|---|---|
| Đăng nhập | `AuthService`, `LoginView`, `account_login_state` | Có BCrypt, ngưỡng 5 lần sai và khóa 5 phút. Cần kiểm tra cả thời điểm hết khóa; thao tác DB vẫn chạy đồng bộ trên UI. |
| Tài khoản/quyền | `AccountService`, `AccountView`, `AccessRules` | Có tạo tài khoản, xem trạng thái và tự đổi mật khẩu; chưa có thao tác quản trị khóa/mở tài khoản hay đổi role sau khi tạo. Test quyền chưa phủ hết service. |
| Căn hộ/hộ | `ApartmentService`, `ApartmentView`, `apartments` | Có tạo/sửa, mã duy nhất, diện tích, ngày và cờ tính phí. Cần thống nhất tên liên hệ/chủ hộ (R-05). |
| Nhân khẩu | `ResidentService`, `ResidentView`, `residents` | Có quan hệ, giấy tờ, tra cứu không dấu và thay chủ hộ có xác nhận; database mới không thêm cư dân mẫu. |
| Loại phí/đơn giá | `FeeService`, `FeeView`, `fee_types`, `fee_rates` | Có lịch sử giá, chọn giá theo đầu kỳ; đã kiểm tra đổi giá giữa tháng trên MySQL ngày 25/11. |
| Kỳ thu | `BillingService`, `AppRepository.issuePeriod`, `charges` | Có transaction, unique kỳ và snapshot số lượng/giá/tiền; cần hồi quy R-03 và kiểm tra trường hợp thiếu đơn giá. |
| Bảng kê/thông báo | `ChargesView`, `FeeNoticePrinter` | Có xem bảng kê, in riêng và in cả kỳ. Test tự động mới kiểm tra chuỗi chuyển khoản; cần thử bố cục, in thật và giữ đúng thông tin lịch sử (R-04). |
| Thanh toán | `CollectionService`, `payments` | Có thu một phần/nhiều lần, tiền mặt/chuyển khoản và nộp dư; mã giao dịch trùng bị từ chối khi ghi qua repository. |
| Công nợ/số dư | `DebtView`, `apartment_credits`, `credit_applications` | Có tổng hợp theo kỳ, lọc trạng thái/tầng và cấn số dư khi phát hành kỳ sau; số dư hiện là số dư hiện tại. |
| Tra cứu/tổng quan | `ResidentService.search`, `DashboardView` | Có tìm kiếm hồ sơ và KPI của kỳ mới nhất theo tháng/năm. Cần đối chiếu tiền cấn trừ, không cộng toàn bộ tiền nhận vào KPI đã thanh toán. |
| Phản hồi giao diện | Các handler `load`, đăng nhập, lưu và phát hành | Bootstrap và gửi trang in có luồng nền; nhiều truy vấn và lệnh lưu khác vẫn chạy trên JavaFX Application Thread (R-06). |

## Fixture kiểm thử

Database mới sau bootstrap không có tài khoản hay dữ liệu nghiệp vụ; lần mở đầu tiên đòi tạo tài khoản quản trị. Bộ test tính phí tự tạo fixture riêng:

- 8 căn A101 đến A108, tổng diện tích 597,75 m²; 18 nhân khẩu, đủ HEAD/WIFE/CHILD/PARENT/TENANT.
- Tài khoản quản trị và kế toán do test tạo. Không có kỳ thu hay thanh toán.
- Trong nhóm bắt buộc chỉ `SERVICE` (10.000 đồng/m²) và `MANAGEMENT` (7.000 đồng/m²) đang áp dụng, hiệu lực từ 01/01/2026.
- Phí vệ sinh và phí xe do test tạo nhưng để tắt. Loại tiện ích sinh ra khi nhập hóa đơn đầu tiên.
- Một kỳ trên fixture nguyên trạng sinh 16 khoản, tổng 10.161.750 đồng. A101 phải thu 816.000 đồng, A102 phải thu 892.500 đồng.

Fixture này không nạp vào database ứng dụng. Database cũ giữ nguyên, không tự thêm những căn hộ, cư dân hay khoản thu còn thiếu.

## Điểm cần xử lý

Các mục dưới xác định qua đối chiếu source; trạng thái chạy thực tế ghi riêng trong [Biên bản kiểm thử](QA_TEST_REPORT.md).

| Mã | Ưu tiên | Hiện trạng và hướng xử lý | Kiểm tra lại |
|---|---|---|---|
| R-02 | Cao | Đã yêu cầu mã giao dịch và dùng khóa MySQL để từ chối mã đã tồn tại ở các lượt ghi qua repository. Ghi SQL trực tiếp hoặc dùng hai mã khác nhau thì cơ chế này không phát hiện. | Test MySQL ngày 25/11: hai yêu cầu đồng thời cùng mã chỉ tạo một phiếu; gửi lại tuần tự bị từ chối. |
| R-03 | Cao | Đã chọn đơn giá có hiệu lực tại ngày đầu tháng của kỳ thu. | Test MySQL ngày 25/11: đổi giá giữa tháng, phát hành sau ngày đổi vẫn lấy giá đầu kỳ. |
| R-04 | Trung bình | `listCharges()` và `listPayments()` join mã căn/tên hộ hiện tại. Cần snapshot thông tin nhận diện chứng từ, hoặc liên kết tới hồ sơ hộ có phiên bản, để tách rõ dữ liệu lịch sử với thông tin liên hệ hiện tại. | FUN-28 |
| R-05 | Trung bình | `household_name` vừa là tên liên hệ vừa được dùng như tên chủ hộ; lưu HEAD có cập nhật sang căn hộ nhưng sửa căn hộ lại có thể ghi khác. Cần chốt một nguồn chủ hộ, hoặc tách trường liên hệ và sửa nhãn. | FUN-03, FUN-04 |
| R-06 | Trung bình | Handler đăng nhập, tải dữ liệu, lưu phiếu và phát hành kỳ gọi JDBC đồng bộ. Cần chuyển sang `Task`/executor, hiện tiến trình, chặn thao tác lặp khi đang chạy và cập nhật UI đúng luồng. | Cần bổ sung case độ trễ; UI-02 mới chỉ kiểm tra hiển thị bảng kê. |

R-04 và R-05 cần chốt thêm mô hình dữ liệu và cách trình bày. Hành vi hiện tại chưa bảo đảm đối soát lịch sử theo thông tin hộ tại lúc phát hành.

## Phần mở rộng theo dõi riêng

- Phương tiện và phí gửi xe.
- Hóa đơn điện, nước, internet.
- Yêu cầu/phản ánh cư dân.
- Đóng góp tự nguyện.
- Xuất CSV.
- Lịch sử biến động cư trú.

Các chức năng này hồi quy riêng được. Menu hiện theo role; source chưa có chế độ tự ẩn các mục ngoài Sprint 1. Khi demo thì giới thiệu rõ phần mở rộng và đừng lấy case mở rộng thay cho luồng Sprint 1.

## Điều kiện chốt nghiệm thu

1. Chốt phạm vi Sprint 1 và các yêu cầu còn chưa rõ với PM.
2. Tiếp tục hồi quy luồng thu phí sau khi sửa R-02/R-03; xác nhận cách dùng mã giao dịch khi thử lại.
3. Chốt chính sách R-04/R-05, kiểm tra chứng từ cũ sau khi đổi hồ sơ; xử lý hoặc ghi nhận quyết định chấp nhận có giới hạn cho các mục trung bình còn mở.
4. Chạy với JDK 25 và MySQL 8.4 trên database demo `bluemoon` nạp từ `schema.sql` và `demo-data.sql`; báo cáo rõ đạt/lỗi/bỏ qua. Test tích hợp bị skip không tính là đã xác minh MySQL.
5. Có bằng chứng thủ công cho UI, cho phân quyền tại service và cho bản in thật; lưu log, ảnh/CSV, SQL đối chiếu theo kế hoạch QA.

## Kịch bản demo luồng chính

1. Trên database demo riêng mới, đăng nhập admin; kiểm tra 8 căn, 18 nhân khẩu và đúng hai khoản bắt buộc đang bật.
2. Phát hành kỳ 10/2026, ngày phát hành 01/10/2026, hạn nộp 15/11/2026; kiểm tra 16 khoản và tổng 10.161.750 đồng.
3. Xem bảng kê A101: dịch vụ 480.000 đồng, quản lý 336.000 đồng, tổng 816.000 đồng. Thử in và đối chiếu bản in.
4. Đăng nhập accountant; thu dịch vụ A101 lần đầu 100.000 đồng, lần hai 380.000 đồng; kiểm tra hai phiếu và dịch vụ hết nợ. Phí quản lý vẫn còn nợ 336.000 đồng.
5. Thu dịch vụ A102 số tiền 600.000 đồng; kiểm tra cấn 525.000 đồng và dư 75.000 đồng. Phí quản lý vẫn còn nợ 367.500 đồng.
6. Đăng nhập admin, phát hành kỳ 11/2026; kiểm tra A102 được cấn 75.000 đồng số dư và số dư còn lại bằng 0.
7. Đối chiếu lịch sử, bảng kê, công nợ và tổng quan theo kỳ đang chọn. Test quyền và case lỗi làm riêng; demo chạy thuận lợi không có nghĩa đã qua toàn bộ kế hoạch QA.
