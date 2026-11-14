# Kế hoạch kiểm thử BlueMoon - Sprint 1 và hồi quy

Kế hoạch kiểm tra luồng căn hộ-nhân khẩu-khoản thu-kỳ thu-thông báo-thanh toán-công nợ. Phương tiện, tiện ích, phản ánh, đóng góp, CSV và biến động cư trú được theo dõi trong nhóm hồi quy mở rộng. Phạm vi Sprint 1 theo bộ tài liệu trong repo; việc đối chiếu với kế hoạch được phê duyệt còn phụ thuộc baseline nêu ở [Đối chiếu Sprint 1](SPRINT1_COVERAGE_REVIEW.md).

## Môi trường và cách chạy

| Thành phần | Yêu cầu |
|---|---|
| Hệ điều hành cho luồng UI | Windows có môi trường JavaFX và máy in/PDF để kiểm tra bản in |
| Java/Maven | JDK 25, Maven Wrapper 3.9.16; kiểm tra Java thực tế bằng `.\mvnw.cmd -version` |
| Database | MySQL 8.4, database kiểm thử riêng có thể tạo lại |
| Test tự động | JUnit 5, kết quả trong `target/surefire-reports/` |
| Cấu hình tích hợp | `BLUEMOON_IT_DB_URL`, `BLUEMOON_IT_DB_USER`, `BLUEMOON_IT_DB_PASSWORD` |
| Cấu hình UI | `BLUEMOON_DB_URL`, `BLUEMOON_DB_USER`, `BLUEMOON_DB_PASSWORD`; đặt riêng nếu chạy ứng dụng trên database QA |

Lệnh chạy và cách tạo MySQL kiểm thử ở cổng 3308 nằm trong [README](../README.md). Test tích hợp thay schema và xóa fixture; không chạy vào database thật hoặc database đang dùng demo. Không chạy nhiều lượt test cùng lúc trên cùng schema.

Lưu phiên bản và log cho mỗi lượt:

```powershell
New-Item -ItemType Directory -Force .\work\qa | Out-Null
git rev-parse HEAD | Set-Content .\work\qa\commit.txt
java -version 2>&1 | Tee-Object -FilePath .\work\qa\java.txt
.\mvnw.cmd -version 2>&1 | Tee-Object -FilePath .\work\qa\maven.txt
.\mvnw.cmd test 2>&1 | Tee-Object -FilePath .\work\qa\tests.txt
```

Lệnh test trên giả định đã đặt đủ biến tích hợp nếu muốn kiểm tra MySQL. Nếu thiếu URL, `MySqlBillingFlowTest` bị skip. Giữ cả kết quả Surefire và log; không suy ra tất cả test đạt chỉ từ dòng `BUILD SUCCESS`.

## Tách bộ dữ liệu

| Bộ | Mục đích | Chuẩn bị và giới hạn |
|---|---|---|
| A - Fixture đối chiếu | Tính phí, thanh toán, công nợ, in và KPI | Chỉ tạo riêng cho kiểm tra: 8 căn, 18 cư dân, hai phí m² đang bật. Database mới sau bootstrap để trống; không dùng fixture này làm dữ liệu mặc định của ứng dụng. |
| B - CRUD | Tạo/sửa hồ sơ, giấy tờ, tài khoản, loại phí | Schema riêng sau bootstrap. Dùng căn `P0508`, mã hộ `HK-QA0508`, phí `QA_SERVICE`, user `qa_accountant`, giấy tờ test chưa dùng. Không dùng tổng tiền bộ A sau khi thêm dữ liệu ở bộ B. |
| C - Mở rộng | Xe, tiện ích, phản ánh, đóng góp, CSV và cư trú | Schema riêng sau bootstrap; chủ động bật phí mở rộng và ghi lại số người/xe/hóa đơn trước phát hành. Không lấy tổng tiền bộ A làm expected cho bộ C. |
| D - Migration | Nâng cấp cấu trúc/dữ liệu cũ | Schema riêng mới chứa đúng fixture cũ trước bootstrap; mỗi lượt migration dùng schema mới hoặc khôi phục bản sao ban đầu. Không seed trước rồi mới bật chế độ legacy. |
| E - Case lỗi và thay đổi giá/hồ sơ | R-02, R-03, R-04, R-05 và thao tác cạnh tranh | Bản sao độc lập của bộ A, khôi phục trước mỗi case làm thay đổi giá, hồ sơ hoặc thanh toán. |

Fixture A phải được chuẩn bị riêng trước khi chạy các case tính phí; database mới sau bootstrap không có A101 hoặc mã phí mẫu. Các fixture trên dùng cho kiểm thử có kiểm soát, không được tạo tự động khi ứng dụng khởi động. Bộ kiểm tra tích hợp chuẩn bị fixture của nó trên database riêng.

### Số liệu chuẩn bộ A

Các đơn giá 10.000 đồng/m² dịch vụ và 7.000 đồng/m² quản lý có hiệu lực từ 01/01/2026. Phát hành kỳ 10/2026 ngày 01/10/2026, hạn 15/11/2026:

| Căn | Diện tích (m²) | Phí dịch vụ (đồng) | Phí quản lý (đồng) | Tổng (đồng) |
|---|---:|---:|---:|---:|
| A101 | 48,00 | 480.000 | 336.000 | 816.000 |
| A102 | 52,50 | 525.000 | 367.500 | 892.500 |
| A103 | 65,00 | 650.000 | 455.000 | 1.105.000 |
| A104 | 70,00 | 700.000 | 490.000 | 1.190.000 |
| A105 | 78,50 | 785.000 | 549.500 | 1.334.500 |
| A106 | 86,25 | 862.500 | 603.750 | 1.466.250 |
| A107 | 92,00 | 920.000 | 644.000 | 1.564.000 |
| A108 | 105,50 | 1.055.000 | 738.500 | 1.793.500 |
| Tổng | 597,75 | 5.977.500 | 4.184.250 | 10.161.750 |

Expected của kỳ: 16 khoản phải thu, 8 hộ, chưa thanh toán. Số tiền trong bảng là phép tính đối chiếu, không phải kết quả của một lần test đã chạy.

## Quy ước ghi kết quả

- **Đạt:** đã chạy đúng fixture, có bằng chứng và khớp expected.
- **Không đạt:** đã chạy, kết quả khác expected; ghi mã lỗi và actual.
- **Bị chặn:** không chạy đến bước xác minh vì thiếu môi trường hoặc bước trước thất bại.
- **Chưa chạy:** mới lập case hoặc mới phân tích source, chưa thực thi.
- **Bỏ qua:** test runner skip/abort, kèm lý do; không cộng vào số đạt.

Các case có ghi “chưa đáp ứng trong code” vẫn giữ expected nghiệp vụ. Không sửa expected theo lỗi để đánh dấu đạt.

## Các case nghiệm thu chính

| Mã | Bộ/tiền điều kiện | Thao tác | Kết quả mong đợi |
|---|---|---|---|
| TC-01 | B, tài khoản QA đang hoạt động | Sai mật khẩu 5 lần; thử đúng ngay sau đó; thử lại sau 5 phút | Bị khóa ở lần sai thứ 5, mật khẩu đúng vẫn bị chặn trong thời gian khóa; sau hết khóa đăng nhập đúng được. UI hiện chỉ báo khóa tạm, chưa hiển thị mốc mở lại cụ thể. |
| TC-02 | B, đã có P0508 | Tạo lại P0508 với mã hộ mới | Bị từ chối, không thêm căn hoặc đổi bản ghi cũ. |
| TC-03 | B, P0508 có một HEAD | Thêm HEAD thứ hai và hủy bước thay chủ hộ; sau đó thử xác nhận thay | Hủy giữ nguyên chủ hộ cũ; xác nhận thay thì chỉ còn một HEAD, người cũ thành OTHER. |
| TC-04 | A, kỳ 10/2026 tồn tại | Phát hành lại cùng tháng | Bị từ chối; số kỳ, số charge và tổng tiền không đổi. |
| TC-05 | A nguyên trạng | Phát hành kỳ 10/2026 | 16 khoản, tổng 10.161.750 đồng; từng căn khớp bảng chuẩn, không có phí mở rộng. |
| TC-06 | E, đã phát hành kỳ 10 | Thêm giá dịch vụ hiệu lực 01/11 rồi mở lại kỳ 10 và phát hành kỳ 11 | Kỳ 10 giữ snapshot; kỳ 11 dùng giá đúng đầu kỳ. Case giá giữa tháng kiểm tra riêng ở RATE-01. |
| TC-07 | A sau TC-05 | Thu dịch vụ A101 100.000 đồng | Dịch vụ còn 380.000, trạng thái PARTIALLY_PAID; tổng nợ hộ A101 còn 716.000; đúng một phiếu. |
| TC-08 | A, dịch vụ A102 chưa thu | Nhận 600.000 đồng cho dịch vụ A102 | Cấn 525.000, dư 75.000; dịch vụ PAID; quản lý vẫn nợ 367.500. Phát hành tháng 11 thì tổng cấn số dư vào kỳ mới là 75.000. |
| TC-09 | B, accountant | Gọi `AccountService.list/create`, `ApartmentService.save`, `BillingService.issue` từ test/service | `SecurityException` trước khi ghi dữ liệu; không chỉ kiểm tra menu bị ẩn. Cần bổ sung test tài khoản vì `AuthorizationTest` hiện chưa có case đó. |
| TC-10 | A có kỳ thu, máy in/PDF sẵn sàng | Xem bảng kê, in một hộ và cả kỳ | Nội dung/số tiền đúng, đủ các hộ và chi tiết, không cắt chữ; theo dõi phản hồi UI và lưu bản in. |

## Ma trận chi tiết và hồi quy

| Mã | Bộ | Bước kiểm tra | Expected / lưu ý hiện tại |
|---|---|---|---|
| AUTH-01 | B | Đăng nhập đúng/sai bằng email, số điện thoại và username tương thích; email viết hoa, số điện thoại có khoảng trắng hoặc dạng +84 | Đúng mới vào shell; email không phân biệt hoa thường, số điện thoại được chuẩn hóa; thông tin sai không tiết lộ tài khoản. |
| AUTH-02 | B | Đổi mật khẩu: sai mật khẩu cũ, xác nhận lệch, mật khẩu quá ngắn/quá dài; thử hợp lệ | Lỗi không đổi hash; dialog vẫn mở để sửa; hợp lệ dùng mật khẩu mới được, mật khẩu cũ bị từ chối. |
| AUTH-05 | B | Từ màn hình ngoài chọn Quên mật khẩu; nhập username, contact không tồn tại, email/số điện thoại hợp lệ, mật khẩu xác nhận lệch rồi hợp lệ | Username không được dùng để khôi phục; contact sai không đổi dữ liệu; contact đúng đổi được mật khẩu và đăng nhập lại được bằng email/số điện thoại. |
| AUTH-03 | B | Thực hiện TC-01, gồm hết hạn khóa | Ghi số lần sai, thời điểm khóa và kết quả sau 5 phút. |
| AUTH-04 | B | Kiểm tra đủ ma trận quyền trong NGHIEP_VU | Chặn thao tác trái quyền tại service; admin không ghi phiếu thu bắt buộc, cả hai role được ghi đóng góp. |
| APT-01 | B | Tạo P0508, HK-QA0508, 80,25 m², ngày hợp lệ | Tạo thành công; tầng 5; không dùng A101 đã tồn tại. |
| APT-02 | B | Trùng mã căn/mã hộ; tầng 0/31; diện tích 0 hoặc hơn 2 chữ số thập phân | Từ chối, dữ liệu cũ giữ nguyên. |
| APT-03 | E | Sửa diện tích sau phát hành, phát hành kỳ sau | Số lượng/tiền kỳ cũ không đổi; kỳ mới dùng diện tích mới. |
| RES-01 | B | Thêm cư dân đầu tiên của P0508 là HEAD, sau đó CHILD | Quan hệ và giấy tờ đúng; cư dân đầu tiên không phải HEAD bị từ chối. |
| RES-02 | B | TC-03; thiếu giấy tờ người 14 tuổi; trùng giấy tờ; chuyển đến trước sinh | Từ chối dữ liệu không hợp lệ; thay chủ hộ chỉ sau xác nhận. |
| RES-03 | A | Tìm `nguyen`, `An`, A101, số giấy tờ đầy đủ, điện thoại, tầng 1 | Kết quả đúng quy tắc tìm kiếm; đối chiếu mã cư dân/căn, không chỉ số dòng. |
| RES-04 | E | Sửa Người liên hệ của căn khác tên cư dân HEAD, mở hồ sơ và công nợ | Cần chốt/tách mô hình để nhãn và nguồn tên nhất quán. Code hiện có thể lệch (R-05); chưa coi là đạt. |
| FEE-01 | B | Tạo QA_SERVICE, bắt buộc, PER_M2, 10.000 đồng, hiệu lực 01/01/2026 | Lưu đúng loại và giá khởi tạo, không trùng SERVICE có sẵn. |
| FEE-02 | B | Giá âm/0, thiếu ngày, trùng ngày hiệu lực; bật/tắt khoản thu | Chặn giá/ngày không hợp lệ; chỉ khoản bật tham gia lần phát hành sau. |
| RATE-01 | E riêng | Giá dịch vụ cũ 10.000; thêm 12.000 từ 15/10/2026; phát hành 20/10/2026 | Theo đầu kỳ A101 phải là 480.000; code hiện tính 576.000 (R-03). Actual này là dự đoán từ source, cần chạy để ghi kết quả. |
| RATE-02 | E riêng | Đơn giá đầu tiên của phí bắt buộc chỉ có từ 15/10; phát hành 20/10 | Theo đầu kỳ phải báo thiếu giá, rollback kỳ và charge; code hiện có thể dùng giá ngày 15 (R-03). |
| BILL-01 | A | TC-05, mở chi tiết từng căn | Đúng quantity, giá, tiền, kỳ và hạn nộp; 8 hộ, 16 khoản. |
| BILL-02 | E | Trùng kỳ hoặc bật một phí chưa có giá hiệu lực | Không sinh dữ liệu dở dang; số liệu kỳ trước không đổi. |
| BILL-03 | A | In/xem theo TC-10 | Lưu PDF/ảnh và kiểm tra từng trang; test chuỗi chuyển khoản không thay cho case in. |
| PAY-01 | A | TC-07 | Đúng 100.000 cấn và 380.000 nợ dịch vụ. |
| PAY-02 | A sau PAY-01 | Thu tiếp dịch vụ A101 380.000 bằng chuyển khoản | Dịch vụ PAID, đúng hai phiếu tổng 480.000; hộ vẫn còn quản lý 336.000. |
| PAY-03 | A | TC-08 | Nhận/cấn/dư tách đúng; số dư 75.000 được dùng trong kỳ sau. |
| PAY-04 | E riêng, dịch vụ A103 nợ 650.000 | Gửi hai lần cùng yêu cầu thu 100.000, cùng mã tham chiếu `QA-DUP-001` | Chỉ một phiếu, đã thu 100.000. Code hiện chưa đáp ứng chống ghi trùng (R-02). |
| PAY-05 | E riêng | Hai yêu cầu đồng thời với cùng mã định danh sau khi triển khai R-02 | Chỉ một phiếu/kết quả thu; có kiểm soát tại database, không chỉ disable nút UI. |
| DEBT-01 | A sau PAY-02/03 | Lọc kỳ, Còn nợ, Đóng thiếu, Đã đóng đủ và tầng | Tổng theo hộ khớp charge; Còn nợ chỉ có balance > 0. Số dư là hiện tại, tách khỏi tiền cấn kỳ đang chọn. |
| DEBT-02 | A | Phát hành kỳ 11 sau khi có credit A102; thử phát hành kỳ trước kỳ nguồn trên bản sao E | 75.000 chỉ cấn kỳ sau kỳ nguồn; kỳ cũ không bị cấn ngược. |
| HIST-01 | E | Phát hành, thu một phần và lưu bản in/lịch sử; đổi mã căn/tên hộ; xem/in lại kỳ cũ và lịch sử thu | Yêu cầu chứng từ giữ thông tin nhận diện thời điểm phát hành; code hiện join hồ sơ mới (R-04). |
| DASH-01 | A sau PAY-02/03, trước kỳ 11 | Đối chiếu KPI kỳ 10 với SQL | Phải thu 10.161.750; đã thanh toán 1.005.000; còn nợ 9.156.750. Tiền nhận 1.080.000, dư 75.000, không cộng dư vào KPI đã thanh toán. |
| CONTRIB-01 | C | Tạo QA_FUND tự nguyện, nhập 0 rồi 50.000 đồng | 0 bị từ chối; khoản hợp lệ lưu riêng; tổng charge/debt bắt buộc không tăng. |
| VEH-01 | C | Đăng ký xe máy/ô tô, thử biển trùng đang hoạt động | Lưu đúng căn/loại, trùng bị từ chối; biển ngừng hoạt động có thể đăng ký lại. |
| VEH-02 | C | Bật phí xe, phát hành; ngừng một xe rồi phát hành kỳ sau | Chỉ xe đang active lúc phát hành được tính; snapshot kỳ đầu giữ nguyên. |
| UTIL-01 | C | Nhập/cập nhật cùng căn/kỳ/loại, rồi phát hành | Chỉ một hóa đơn theo bộ khóa, charge dùng số tiền cuối trước phát hành. |
| UTIL-02 | C | Sửa/xóa hóa đơn sau khi kỳ đã phát hành | Bị chặn, hóa đơn và charge giữ nguyên. |
| REQ-01 | C | Tạo yêu cầu thiếu nội dung; tạo hợp lệ; cập nhật xử lý/hoàn tất | Chặn thiếu dữ liệu; yêu cầu mới RECEIVED; lưu trạng thái và ghi chú mới, còn tìm được bản ghi hoàn tất. |
| EXP-01 | C | Xuất 4 loại bảng, gồm dữ liệu có dấu, dấu phẩy, xuống dòng và tiền | Đủ dòng/cột đang hiển thị, UTF-8/BOM, tiền giữ dạng số; cảnh báo ghi đè; thử nội dung bắt đầu bằng dấu công thức. |
| CHANGE-01 | C | Ghi tạm trú/tạm vắng hợp lệ và khoảng ngày đảo | Lưu lịch sử hợp lệ, chặn ngày kết thúc trước bắt đầu; chưa mong đợi lịch sử này làm thay đổi số người tính phí. |
| MIG-01 | D | Fixture cũ A101-A103, chưa có cư dân; bootstrap | Giữ nguyên ba căn cũ, không tự thêm tài khoản, cư dân hoặc phí mẫu; vẫn cho phép tạo tài khoản quản trị đầu tiên. |
| MIG-02 | D riêng | Fixture cũ có dữ liệu thanh toán, thiếu payer_name/applied_amount; nâng cấp hai lần | Giữ giao dịch, bổ sung trường và constraint; lần hai không tạo dữ liệu lặp. Không dùng fixture thiếu căn để che lấp lỗi migration thanh toán. |
| UI-01 | E | Đăng nhập/tải/lưu/phát hành khi DB có độ trễ trong môi trường test | Có trạng thái đang xử lý, UI phản hồi, không lưu lặp; nhiều handler hiện còn đồng bộ (R-06). |
| UI-02 | A hoặc C | In toàn kỳ với dữ liệu đủ dài và thử hủy in | Không mất dữ liệu, hủy đúng; kiểm tra cắt chữ, bố cục, phản hồi UI. |

Các mã trong ma trận là kế hoạch kiểm thử, không có nghĩa đã tồn tại một phương thức JUnit cho từng mã.

## Luồng liên kết dữ liệu

- **E2E-01 - Sprint 1:** bộ A → BILL-01 → PAY-01 → PAY-02 → PAY-03 → DEBT-01 → DASH-01 → phát hành tháng 11 → DEBT-02. Giữ log/CSV để theo được cùng căn, kỳ và khoản từ đầu đến cuối.
- **E2E-02 - Mở rộng:** bộ C → VEH-01/02 + UTIL-01 → phát hành → UTIL-02 → REQ-01 + CONTRIB-01 + EXP-01. Không sửa expected của bộ A để dùng cho bộ C.
- **E2E-03 - CRUD:** bộ B → APT-01 → RES-01/02 → FEE-01 → phát hành một kỳ chưa có, kiểm tra P0508-QA_SERVICE xuất hiện đúng. Expected được tính từ bộ B thực tế, gồm cả các khoản seed còn bật; không áp tổng 16 charge của bộ A.

## Truy vấn đối chiếu

Chạy trong đúng schema QA. Các truy vấn sau chỉ đọc dữ liệu:

```sql
SELECT COUNT(*) AS apartment_count FROM apartments;
SELECT COUNT(*) AS resident_count FROM residents;
SELECT code, required, calculation_basis, active
FROM fee_types ORDER BY code;

SELECT p.period_year, p.period_month, COUNT(c.id) AS charge_count,
       COALESCE(SUM(c.amount), 0) AS total_amount
FROM billing_periods p
LEFT JOIN charges c ON c.billing_period_id = p.id
GROUP BY p.id, p.period_year, p.period_month
ORDER BY p.period_year, p.period_month;

SELECT c.id, a.code, f.code AS fee_code, c.quantity_snapshot,
       c.unit_price_snapshot, c.amount,
       (SELECT COALESCE(SUM(pm.applied_amount), 0)
        FROM payments pm WHERE pm.charge_id = c.id)
       + (SELECT COALESCE(SUM(ca.amount), 0)
          FROM credit_applications ca WHERE ca.charge_id = c.id) AS paid
FROM charges c
JOIN billing_periods p ON p.id = c.billing_period_id
JOIN apartments a ON a.id = c.apartment_id
JOIN fee_types f ON f.id = c.fee_type_id
WHERE p.period_year = 2026 AND p.period_month = 10
ORDER BY a.code, f.code;

SELECT pm.id, a.code, pm.payer_name, pm.reference, pm.paid_at,
       pm.amount, pm.applied_amount,
       pm.amount - pm.applied_amount AS credit_created
FROM payments pm
JOIN charges c ON c.id = pm.charge_id
JOIN apartments a ON a.id = c.apartment_id
ORDER BY pm.id;

SELECT a.code, SUM(ac.remaining_amount) AS credit_remaining
FROM apartment_credits ac
JOIN apartments a ON a.id = ac.apartment_id
GROUP BY a.id, a.code;
```

Không join trực tiếp nhiều dòng `payments` với nhiều dòng `credit_applications` rồi cộng, vì có thể nhân bản số tiền. KPI đã thanh toán lấy tổng cấn trừ, không lấy tổng `payments.amount`.

## Bằng chứng và điều kiện kết thúc

Mỗi lượt cần commit SHA, người chạy, ngày giờ/múi giờ, phiên bản Java/MySQL/Maven, tên schema, bộ fixture, lệnh chạy, số test đạt/lỗi/bỏ qua và vị trí log. Không đưa mật khẩu hoặc chuỗi kết nối có thông tin bí mật vào log chia sẻ.

Với case lỗi, ghi đủ tiền điều kiện, bước tái hiện, actual, expected, mức độ, mã R liên quan và bằng chứng. Với case in/UI, lưu ảnh/PDF và nhận xét bố cục/phản hồi; với BILL/PAY/DEBT/DASH lưu SQL hoặc CSV đối chiếu.

Chỉ chốt nghiệm thu khi đã xử lý các mục ưu tiên cao, chốt quyết định cho mục còn mở, chạy lại case liên quan và hồi quy đầy đủ trên môi trường đúng. Case chưa chạy, bị chặn hoặc bị skip phải giữ nguyên trạng thái trong [Biên bản kiểm thử](QA_TEST_REPORT.md).
