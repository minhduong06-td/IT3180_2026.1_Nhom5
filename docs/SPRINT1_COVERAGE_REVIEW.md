# Sprint 1 coverage review

Ngày rà soát: 2026-11-10
Phạm vi: mã nguồn hiện tại của `IT3180_2026.1_Nhom5-main`, các tài liệu kế hoạch ở thư mục cha và luồng demo Sprint 1.

## Kết luận ngắn

Luồng dọc tối thiểu của Sprint 1 đã có trong mã nguồn:

`đăng nhập → căn hộ/hộ → nhân khẩu → khoản thu/đơn giá → phát hành kỳ thu → bảng kê/thông báo → ghi nhận thanh toán → công nợ/lịch sử`.

Tuy nhiên, không nên kết luận “toàn bộ kế hoạch đã hoàn tất” chỉ vì các màn hình đã tồn tại. Có ba điểm cần giữ trong nghiệm thu:

1. Nhiều chức năng Sprint 2/backlog đã được lập trình sẵn trong source. Chúng là phần code vượt phạm vi, không được tính vào Definition of Done của Sprint 1.
2. Dữ liệu khởi tạo cũ chỉ có ba căn hộ và không có nhân khẩu, khiến demo thực tế bị rỗng. Đã bổ sung tám căn hộ và mười tám nhân khẩu mẫu trong `DatabaseBootstrap`.
3. Cơ chế phát hành lấy các khoản thu đang active và `required = TRUE`. Bootstrap mới giữ fixture Sprint 1 chỉ bật hai khoản theo m² (Phí dịch vụ và Phí quản lý); các khoản vệ sinh/xe vẫn có thể xem trong danh mục nhưng mặc định tắt để không lọt vào kỳ thu Sprint 1.

## Nguồn và cách đối chiếu

- Kế hoạch: `Ke_Hoach_Agile.xlsx`, `WBS.xlsx`, `Project_Charter.xlsx`, `Theo_Doi_Rui_Ro.xlsx`, `Dashboard.xlsx` ở thư mục dự án cha.
- Baseline Sprint 1 đã diễn giải: `Sprint1.md` ở thư mục dự án cha.
- Nghiệp vụ và quy tắc hiện thực: `docs/NGHIEP_VU.md`.
- Kiểm thử: `docs/QA_TEST_PLAN.md`, `docs/QA_TEST_REPORT.md`, các test trong `src/test/java`.
- Mã nguồn dùng để xác minh: `src/main/java/com/bluemoon/service`, `src/main/java/com/bluemoon/dao/AppRepository.java`, `src/main/java/com/bluemoon/ui`, `src/main/resources/db/schema.sql`.

Repo `IT3180-2025I-SE_09-main` không có bản checkout trong workspace hiện tại. Phần so sánh Repo09 dưới đây dùng kết quả rà soát Repo09 đã thực hiện trước đó; không coi đây là bằng chứng của một lần build lại Repo09 trong lượt này.

## Ma trận coverage theo Sprint 1

| Story / năng lực | Bằng chứng trong source | Trạng thái thực tế | Nhận xét nghiệm thu |
|---|---|---|---|
| Đăng nhập | `AuthService`, `LoginView`, `account_login_state`, `AuthorizationTest` | Đủ cho Sprint 1 | Có BCrypt, session user và khóa tạm thời sau nhiều lần sai. Cần demo cả admin và accountant. |
| Quản lý tài khoản/quyền | `AccountService`, `AccountView`, `AccessRules`, `Role.ADMIN/ACCOUNTANT` | Đủ ở mức tối thiểu | Có tạo/khóa hoặc mở tài khoản và đổi mật khẩu. Quyền phải được kiểm tra ở service, không chỉ ẩn menu. |
| Căn hộ/hộ khẩu | `ApartmentService`, `ApartmentView`, bảng `apartments` | Đủ | Hồ sơ hộ đang gắn với căn hộ: mã hộ, chủ hộ, diện tích, liên hệ, ngày vào ở, trạng thái. Không có một module “hộ khẩu” độc lập là chấp nhận được nếu nhóm thống nhất mô hình này. |
| Nhân khẩu | `ResidentService`, `ResidentView`, bảng `residents` | Đủ | Có chủ hộ duy nhất, quan hệ, ngày sinh, giới tính, giấy tờ, tìm kiếm. Dữ liệu seed mới có nhiều hộ một người/nhiều người để demo. |
| Loại khoản thu và đơn giá | `FeeService`, `FeeView`, `fee_types`, `fee_rates` | Đủ | Có basis và ngày hiệu lực. Đơn giá được snapshot vào charge khi phát hành. Cần chốt fixture chỉ gồm hai khoản Sprint 1. |
| Kỳ thu và tính phí | `BillingService`, `AppRepository.issuePeriod`, `billing_periods`, `charges` | Đủ về luồng lõi | Có unique kỳ, tính theo m²/nhân khẩu/xe và snapshot. Việc tự động kéo thêm các khoản required của v2 là rủi ro phạm vi Sprint 1. |
| Bảng kê/thông báo | `ChargesView`, `FeeNoticePrinter`, test in thông báo | Đủ ở mức Sprint 1 | Có xem trước/in; chưa phải gửi email/SMS/QR điện tử, đúng với phạm vi đã chốt. |
| Ghi nhận thanh toán | `CollectionService`, `TransactionsView`, `payments` | Đủ | Có tiền mặt/chuyển khoản, người nộp, tham chiếu, thanh toán một phần/nhiều lần. |
| Công nợ/số dư | `DebtView`, `apartment_credits`, `CollectionService` | Đủ | Có trạng thái, số đã thu, còn nợ và nộp dư. Cần đối chiếu dashboard với bảng charge/payment khi demo. |
| Tra cứu/thống kê cơ bản | `ResidentService.search`, `ChargesView`, `DebtView`, `DashboardView` | Đủ tối thiểu | Có tìm kiếm theo dữ liệu chính và KPI cơ bản. Chưa nên yêu cầu báo cáo PDF/Excel hoàn chỉnh trong Sprint 1. |

## Những gì source đang có nhưng không được tính là Sprint 1

Các module sau đã tồn tại trong source, nhưng kế hoạch Agile đưa chúng sang Sprint 2 hoặc backlog:

- đăng ký phương tiện và phí xe;
- nhập hóa đơn điện, nước, internet;
- yêu cầu/phản ánh cư dân;
- khoản đóng góp tự nguyện;
- xuất CSV;
- lịch sử biến động cư trú.

Đây là tình trạng “code vượt kế hoạch”, không phải lý do để mở rộng acceptance scope. Khi demo Sprint 1, nhóm nên ẩn hoặc ghi nhãn “Backlog/Sprint 2” trên kịch bản demo; không dùng các màn hình này để thay thế các bước thu phí lõi.

## Điểm còn thiếu hoặc cần hoàn thiện trước khi chốt Sprint 1

### 1. Fixture thu phí phải tách khỏi dữ liệu v2

`AppRepository.issuePeriod` lấy toàn bộ fee type đang active và `required = TRUE`. `DatabaseBootstrap.seedFees` vẫn tạo metadata và đơn giá cho phí vệ sinh, xe máy, xe ô tô để không mất khả năng phát triển v2, nhưng các khoản này mặc định `active = FALSE` trong database sạch. Vì vậy, kỳ Sprint 1 chỉ sinh Phí dịch vụ và Phí quản lý.

Khi kiểm thử v2, test tích hợp bật rõ các fee type mở rộng trong fixture rồi tắt lại ở phần cleanup. Khi dùng database cũ đã từng được bootstrap bằng bản source trước, không nên tự động tắt dữ liệu đang vận hành; hãy tạo database demo sạch hoặc kiểm tra trạng thái active trước khi demo.

Trước đây đây là điểm lệch phạm vi; hiện đã được xử lý cho database sạch. Nhóm vẫn phải kiểm tra:

- danh mục fee có đúng hai khoản active bắt buộc;
- kỳ phát hành không sinh charge xe/vệ sinh khi chưa bật phần mở rộng;
- các test v2 chỉ bật chúng trong phạm vi hồi quy.

Không được vừa nói Sprint 1 chỉ có hai khoản, vừa demo một kỳ tự sinh thêm phí v2.

### 2. Dữ liệu mẫu không thay thế thao tác nghiệp vụ

Seed mới làm cho màn hình căn hộ/nhân khẩu có dữ liệu đa dạng. Kỳ thu, bảng kê, thanh toán và công nợ vẫn nên được tạo qua luồng ứng dụng trong demo để chứng minh chức năng, không chèn sẵn dữ liệu rồi chỉ mở màn hình kết quả.

Bộ seed hiện có:

- tám căn A101–A108 với diện tích từ 48,00 đến 105,50 m²;
- mười tám nhân khẩu;
- nhiều quan hệ HEAD/WIFE/HUSBAND/CHILD/PARENT/TENANT;
- nhiều ngày vào ở, giới tính, độ tuổi và số giấy tờ khác nhau;
- hai tài khoản `admin` và `accountant`.

Khi chạy trên database đã có dữ liệu, bootstrap không chèn lại nhân khẩu; muốn xem đúng bộ seed phải dùng database sạch.

### 3. Thiếu bằng chứng nghiệm thu cho từng bước

`QA_TEST_PLAN.md` đã có ma trận mở rộng, nhưng khi chạy thật cần lưu ảnh/log và truy vấn đối chiếu cho:

- công thức từng charge;
- snapshot đơn giá sau khi thay đổi đơn giá;
- thanh toán một phần và thanh toán đủ;
- nộp dư thành credit;
- quyền accountant không được quản lý tài khoản/cấu hình;
- dashboard khớp tổng charge, payment và debt.

Một màn hình hiển thị được dữ liệu không đủ chứng minh nghiệp vụ đã đúng.

## So sánh khách quan với Repo09

| Khía cạnh | BlueMoon hiện tại | Repo09 theo lần rà soát trước | Đánh giá cho Sprint 1 |
|---|---|---|---|
| Vai trò | ADMIN và ACCOUNTANT, phù hợp ban quản trị | Có nhiều luồng/người dùng hơn | BlueMoon không thiếu role bắt buộc của đề; thêm resident/officer lúc này là mở rộng phạm vi. |
| Hộ/căn hộ/nhân khẩu | Có hồ sơ căn hộ, chủ hộ, nhân khẩu, tìm kiếm và biến động cư trú | Có dữ liệu quy mô lớn hơn | BlueMoon đủ chức năng; Repo09 nổi bật hơn về quy mô dữ liệu demo. |
| Khoản thu/kỳ thu | Có basis, đơn giá hiệu lực, snapshot và chống charge trùng | Có invoice/payment/report phong phú | BlueMoon có nền domain phù hợp, cần làm rõ fixture Sprint 1. |
| Thanh toán | Có partial payment, nhiều giao dịch và credit nộp dư | Có thêm luồng thanh toán/mock gateway | Gateway/QR không thuộc Sprint 1; không phải gap cần bổ sung. |
| Phương tiện/tiện ích | BlueMoon đã có entity và service riêng | Repo09 cũng có luồng tương ứng | Đã vượt Sprint 1 ở cả hai; không dùng để đánh giá thiếu Sprint 1. |
| Báo cáo/xuất dữ liệu | Có dashboard và CSV | Repo09 có nhiều export/report hơn, gồm PDF/Excel theo lần rà soát | Có thể là backlog sau Sprint 1, không phải thiếu acceptance hiện tại. |
| Dữ liệu demo | Trước đây chỉ 3 căn, không nhân khẩu; đã sửa thành 8 căn/18 nhân khẩu | Repo09 có bộ seed lớn hơn | Bộ mới đủ cho demo lớp học; chưa phải dữ liệu tải/năng lực production. |
| Tính đúng nghiệp vụ | Có snapshot, trạng thái nợ, credit và test unit/integration | Repo09 từng có điểm cần kiểm tra về đơn vị phí xe và cấu hình | BlueMoon có nền kiểm soát tốt; vẫn phải chạy MySQL thật để xác nhận migration/seed. |

### Kết luận so sánh

Không có feature bắt buộc nào của Repo09 cần kéo thêm vào Sprint 1 chỉ vì Repo09 có nó. Những phần Repo09 hơn chủ yếu là quy mô seed, report/export và các luồng mở rộng. Việc cần sửa ngay của BlueMoon là tính nhất quán giữa acceptance Sprint 1 và các khoản `required` được seed, cùng với bằng chứng test end-to-end.

## Kịch bản demo Sprint 1 khuyến nghị

1. Đăng nhập `admin`; mở danh sách căn hộ và nhân khẩu để chứng minh dữ liệu seed.
2. Mở khoản thu; kiểm tra hai khoản theo m² và ngày hiệu lực.
3. Tạo kỳ 10/2026; kiểm tra công thức một căn nhỏ và một căn lớn.
4. Mở bảng kê/thông báo của A101; đối chiếu tổng tiền và thử xem trước/in.
5. Đăng nhập `accountant`; ghi nhận một khoản thanh toán một phần bằng tiền mặt.
6. Ghi nhận phần còn lại bằng chuyển khoản; kiểm tra trạng thái `PAID` và lịch sử.
7. Dùng một căn khác để thử nộp dư; kiểm tra credit và dashboard.
8. Thử truy cập màn hình quản lý tài khoản bằng accountant; phải bị chặn ở service layer.

Các màn hình xe, utility, yêu cầu cư dân, đóng góp và biến động cư trú chỉ mở khi cô/khách hàng hỏi về phần mở rộng; không đưa vào luồng acceptance Sprint 1.
