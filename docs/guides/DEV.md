# Ghi chú cho dev

## Luồng code

| Thành phần | Trách nhiệm |
|---|---|
| `ui/BlueMoonApplication` | Khởi động schema, chọn màn hình tạo quản trị hoặc đăng nhập. |
| `ui/*View`, `MainShell`, `Ui` | Nhập liệu, điều hướng theo vai trò, hiển thị bảng và thông báo. |
| `service/*Service`, `AccessRules` | Kiểm tra quyền, dữ liệu nhập và quy tắc nghiệp vụ trước khi gọi DAO. |
| `service/BillingRules` | Công thức tiền, làm tròn và trạng thái khoản thu. |
| `dao/AppRepository` | JDBC, transaction, truy vấn và khóa dữ liệu. |
| `config/DatabaseBootstrap` | Tạo bảng và nâng cấp các cấu trúc được hỗ trợ. |
| `resources/db/schema.sql` | Bảng, khóa ngoại, chỉ mục và constraint. |
| `model/Domain` | Các record và enum dùng giữa UI, service và DAO. |

Ví dụ phát hành kỳ: `BillingView → BillingService.issue → AppRepository.issuePeriod`. Thu tiền: `ChargesView → CollectionService → repository`. Ẩn menu trên giao diện chỉ để người dùng đỡ nhầm; chặn quyền thật nằm ở service.

## Khởi tạo với database rỗng

Bootstrap không tìm căn hộ, cư dân, phí hay quản trị theo mã cố định. Nó tạo bảng rồi kiểm tra đã có user chưa. Chưa có thì màn hình thiết lập gọi `AccountService.createInitialAdmin`, kiểm tra dữ liệu nhập, băm BCrypt và tạo quản trị.

Hai ứng dụng chạy cùng lúc không tạo được hai tài khoản đầu tiên: repository lấy khóa MySQL `GET_LOCK`, kiểm tra lại trong transaction, rồi ghi user và trạng thái đăng nhập trong cùng transaction. Có user rồi thì bước thiết lập trả lỗi.

Database được dùng trong quy trình hiện tại là database demo do Docker Compose khởi tạo. Dữ liệu nghiệp vụ được nạp từ `database/demo-data.sql`; test không được tự chuyển sang database rỗng.

Cấu hình Docker nạp `schema.sql` rồi `database/demo-data.sql` khi tạo volume mới. Đây là nguồn schema và dữ liệu duy nhất trong quy trình demo và QA hiện tại.

## Snapshot khi phát hành

Số tiền đã phát hành phải giữ nguyên khi hồ sơ hoặc đơn giá đổi. Mỗi charge lưu cách tính, số lượng, đơn giá và thành tiền. Tiền làm tròn về đồng với `HALF_UP`.

Snapshot chưa có mã căn và tên hộ. Hai trường này vẫn join từ hồ sơ tại lúc đọc, nên đổi tên hộ sẽ đổi luôn chứng từ cũ (R-04).

## Transaction và khóa

Phát hành kỳ gồm tạo kỳ, tạo charge và phân bổ số dư; thu tiền gồm ghi phiếu và ghi phần dư. Ghi được một nửa thì tổng nợ và lịch sử lệch nhau. Vì vậy repository commit cả thao tác hoặc rollback. Constraint và khóa dòng xử lý các trường hợp tranh chấp mà validate ở Java không bắt được.

Thu tiền bắt buộc có mã giao dịch. Repository lấy advisory lock chung, kiểm tra mã đã tồn tại chưa, ghi trong transaction rồi nhả khóa; gửi lại cùng mã bị từ chối. Khóa này chỉ bảo vệ đường ghi qua repository: SQL chạy trực tiếp thì không, và hai mã khác nhau cho cùng một giao dịch cũng không phát hiện được.

## Tiền nhận, tiền cấn, số dư

- Tiền nhận: số tiền trên phiếu thu.
- Tiền cấn: phần dùng thanh toán charge được chọn.
- Số dư: phần tiền nhận vượt số còn nợ.
- Đã thanh toán của charge: tiền cấn từ phiếu và từ số dư.
- Còn nợ: tiền phải thu trừ tổng đã cấn.

Số dư chỉ dùng được khi phát hành kỳ sau kỳ nguồn. Thu dư sau khi kỳ sau đã phát hành thì các charge cũ không được phân bổ lại. Xem ví dụ trong [tài liệu PM](PM.md) và cách kiểm tra trong [hướng dẫn QA](../qa/QA.md).

## Ngày chọn đơn giá

Yêu cầu: lấy giá hiệu lực gần nhất nhưng không sau ngày đầu kỳ. Truy vấn trong `issuePeriod` dùng ngày đầu tháng, và expected của `MySqlBillingFlowTest` phải sửa cùng lúc.

Ví dụ phân biệt: căn 48 m², giá 10.000 đồng từ đầu tháng, 12.000 đồng từ ngày 15, phát hành ngày 20. Khoản diện tích của kỳ đó là 480.000 đồng. Lấy giá ngày phát hành sẽ ra 576.000 đồng, và đó là lỗi cần sửa chứ không phải expected mới.

## Quy ước UI

Tiếng Việt ngắn, nhãn hành động cụ thể, tên nghiệp vụ thống nhất. Phụ đề chỉ giữ khi nói thêm quy tắc người dùng cần biết. Giữ nhãn trường, điều kiện nhập, thông tin tiền, hậu quả thao tác; bỏ slogan và câu lặp lại tiêu đề. Style dùng chung trong `resources/ui/app.css`.

Đổi chữ hoặc layout thì mở lại màn hình đăng nhập, thiết lập ban đầu, tổng quan, bảng dữ liệu, lỗi và cửa sổ 1080 × 700. Compile không bắt được chữ bị cắt, mất focus hay menu tràn ra ngoài.

## Quy trình một thay đổi

1. Tạo nhánh có tên mô tả chức năng hoặc lỗi từ main đã cập nhật.
2. Đọc caller, service, truy vấn, schema và test liên quan trước khi sửa.
3. Commit theo từng thay đổi giải thích được và review độc lập được; không gom cả dự án, cũng không chia nhỏ chỉ để tăng số commit.
4. Chạy kiểm tra phù hợp, lưu lệnh và kết quả, ghi rõ mục chưa kiểm tra.
5. Mở PR với vấn đề, hành vi sau sửa, ảnh hưởng dữ liệu và bằng chứng.
6. Review diff rồi merge bằng merge commit.

Không commit `.tools`, `target`, thông tin riêng hoặc `TECH_STACK.md`. Test tích hợp phải dùng đúng database demo theo [QA](../qa/QA.md), không tự tạo database khác.
