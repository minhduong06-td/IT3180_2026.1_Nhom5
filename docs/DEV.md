# Giải trình dành cho dev

## Code đi theo luồng nào?

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

Ví dụ phát hành kỳ: `BillingView → BillingService.issue → AppRepository.issuePeriod`. Thu tiền đi từ `ChargesView` qua `CollectionService` tới repository. Ẩn menu là hỗ trợ sử dụng; service mới là nơi bắt buộc chặn sai vai trò.

## Tại sao database mới chạy được khi chưa có dữ liệu?

Bootstrap không tìm căn hộ, cư dân, phí hay quản trị có mã cố định. Sau khi tạo bảng, ứng dụng kiểm tra có user hay chưa. Màn hình thiết lập gọi `AccountService.createInitialAdmin` để kiểm tra thông tin, băm BCrypt và tạo quản trị.

Repository dùng khóa MySQL `GET_LOCK` và kiểm tra lại trong transaction để hai ứng dụng không cùng tạo hai tài khoản đầu tiên. User và trạng thái đăng nhập được lưu cùng transaction. Khi đã có user, bước thiết lập bị từ chối.

Khi dùng database rỗng, đơn giá, căn hộ và cư dân do người dùng thêm. Loại phí tiện ích được tạo khi lưu hóa đơn đầu tiên. Dữ liệu phục vụ test nằm trong mã test, không chạy cùng bootstrap.

Cấu hình Docker hiện nạp `schema.sql` và `database/demo-data.sql` khi tạo volume mới. Đây là bước khởi tạo của Docker, tách khỏi Java bootstrap; kết nối tới một database rỗng khác vẫn dùng luồng tạo quản trị như trên.

## Tại sao lưu snapshot khi phát hành?

Số tiền đã phát hành phải giữ nguyên khi hồ sơ hoặc đơn giá thay đổi. Mỗi charge lưu cách tính, số lượng, đơn giá và thành tiền. Tính phí định kỳ làm tròn về đồng với `HALF_UP`.

Snapshot hiện chưa gồm mã căn và tên hộ; các trường này còn join hồ sơ hiện tại (R-04). Không giải thích rằng toàn bộ chứng từ đã bất biến.

## Vì sao phải dùng transaction?

Phát hành kỳ gồm tạo kỳ, charge và phân bổ số dư. Thu tiền gồm lưu phiếu và phần dư. Nếu chỉ một phần được ghi thành công, tổng nợ và lịch sử có thể lệch nhau. Repository cần commit cả thao tác hoặc rollback khi thất bại; constraint và khóa dữ liệu bảo vệ những trường hợp cạnh tranh mà validation ở Java không đủ xử lý.

Khóa một bản ghi không tự chống gửi lại yêu cầu. R-02 vẫn cần mã yêu cầu duy nhất, lưu và trả lại kết quả cũ khi nhận lại cùng yêu cầu, cùng kiểm tra cạnh tranh.

## Phân biệt tiền nhận, tiền cấn và số dư

- Tiền nhận: số tiền trên phiếu thu.
- Tiền cấn: phần dùng thanh toán charge được chọn.
- Số dư: phần tiền nhận vượt số còn nợ.
- Đã thanh toán của charge: tiền cấn từ phiếu và từ số dư.
- Còn nợ: tiền phải thu trừ tổng đã cấn.

Số dư chỉ được dùng khi phát hành kỳ sau kỳ nguồn. Việc thu dư sau khi kỳ sau đã phát hành không tự làm phân bổ lại các charge cũ. Xem ví dụ trong [tài liệu PM](PM.md) và kiểm tra bằng [kịch bản QA](QA.md).

## Khi sửa ngày chọn đơn giá cần sửa ở đâu?

Yêu cầu là giá hiệu lực gần nhất nhưng không sau ngày đầu kỳ. Repository hiện dùng ngày phát hành. Cần sửa truy vấn trong `issuePeriod` cùng expected của `MySqlBillingFlowTest`.

Một case phân biệt: căn 48 m², giá 10.000 đồng từ đầu tháng và 12.000 đồng từ ngày 15, phát hành ngày 20. Theo yêu cầu, khoản diện tích của kỳ đó là 480.000 đồng. Kết quả hiện tại 576.000 đồng là lỗi, không phải expected mới để cho test đạt.

## Sửa UI theo quy ước nào?

Dùng tiếng Việt ngắn, nhãn hành động cụ thể và tên nghiệp vụ thống nhất. Phụ đề chỉ tồn tại khi bổ sung quy tắc cần biết. Giữ nhãn trường, điều kiện nhập, thông tin tiền và hậu quả của thao tác; bỏ slogan và đoạn lặp lại tiêu đề. Style dùng chung trong `resources/ui/app.css`.

Thay chữ hoặc layout phải kiểm tra màn hình đăng nhập, thiết lập ban đầu, tổng quan, bảng dữ liệu, lỗi và cửa sổ tối thiểu 1080 × 700. Compile không phát hiện cắt chữ, mất focus hoặc menu nằm ngoài cửa sổ.

## Làm một thay đổi và đưa vào main

1. Tạo nhánh có tên mô tả chức năng hoặc lỗi từ main đã cập nhật.
2. Đọc caller, service, truy vấn, schema và test liên quan trước khi sửa.
3. Commit theo từng thay đổi có thể giải thích và review độc lập; không gom cả dự án, không chia chỉ để tăng số commit.
4. Chạy kiểm tra phù hợp, lưu lệnh và kết quả, ghi rõ mục chưa kiểm tra.
5. Mở PR với vấn đề, hành vi sau sửa, ảnh hưởng dữ liệu và bằng chứng.
6. Review diff rồi merge bằng merge commit mặc định theo quy trình repo.

Không commit `.tools`, `target`, thông tin riêng hoặc `TECH_STACK.md`. Không chạy integration test vào database đang dùng. Hướng dẫn dựng schema riêng nằm trong [QA](QA.md).
