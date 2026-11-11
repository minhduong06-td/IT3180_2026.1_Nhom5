# Hướng dẫn nghiệp vụ BlueMoon

Tài liệu hướng dẫn sử dụng luồng quản lý thu phí và mô tả các quy tắc đang được áp dụng. Những yêu cầu chưa được code đáp ứng được ghi riêng để làm căn cứ sửa và kiểm thử lại.

## Vai trò và quyền hạn

| Thao tác | Ban quản trị (`ADMIN`) | Kế toán (`ACCOUNTANT`) |
|---|---|---|
| Xem tổng quan, hồ sơ căn hộ và nhân khẩu | Có | Có; hồ sơ căn hộ xem qua mục Cư dân |
| Thêm/sửa căn hộ, nhân khẩu | Có | Không |
| Xem danh mục khoản thu và danh sách kỳ qua service | Có | Có; không có menu quản lý riêng |
| Tạo khoản thu, cập nhật đơn giá, bật/tắt khoản thu, phát hành kỳ | Có | Không |
| Xem bảng kê, công nợ, in thông báo và lịch sử thu | Có | Có |
| Ghi nhận thanh toán cho khoản phải thu | Không | Có |
| Xem và ghi nhận đóng góp tự nguyện | Có | Có |
| Quản lý phương tiện, hóa đơn tiện ích và yêu cầu cư dân | Có | Không |
| Ghi nhận biến động cư trú | Có | Không |
| Xem biến động cư trú qua service | Có | Có; menu hiện chỉ dành cho ban quản trị |
| Xem danh sách và tạo tài khoản | Có | Không |
| Tự đổi mật khẩu | Có | Có |

Quyền được kiểm tra tại service, đồng thời giao diện chỉ hiển thị các menu phù hợp. Tài khoản hiện có chức năng tạo, xem trạng thái và tự đổi mật khẩu. Chưa có thao tác quản trị để chủ động khóa/mở tài khoản hoặc đổi vai trò sau khi tạo; khóa đăng nhập 5 phút sau 5 lần sai mật khẩu là cơ chế tự động riêng.

## Thông tin quản lý

| Nhóm | Trường và quy tắc chính |
|---|---|
| Căn hộ/hộ | Mã căn gồm một chữ cái và 3–4 chữ số, như A101 hoặc P1002; tầng lấy từ phần số trước hai chữ số cuối và nằm trong 1–30. Mã căn và mã hộ khẩu duy nhất. Mã hộ có dạng `HK-...`. Diện tích dương, tối đa hai chữ số thập phân; ngày nhận nhà không ở tương lai. Có tình trạng đang ở/trống/cho thuê và cờ tính phí các kỳ mới riêng. |
| Nhân khẩu | Họ tên 2–50 ký tự chữ và khoảng trắng; ngày sinh trước ngày hiện tại; ngày chuyển đến không trước ngày sinh và không ở tương lai. Người từ 14 tuổi cần CCCD/CMND 9 hoặc 12 chữ số; số giấy tờ không trùng. Cư dân đầu tiên của hộ phải là chủ hộ. Khi thay chủ hộ có xác nhận, chủ hộ cũ chuyển sang quan hệ Khác. |
| Phương tiện | Căn hộ đang hoạt động, loại xe và biển số. Biển số được chuẩn hóa; không trùng biển số đang hoạt động. Xe đã ngừng đăng ký được giữ trong dữ liệu và không tính ở lần phát hành sau. |
| Hóa đơn tiện ích | Một hóa đơn cho mỗi căn/kỳ/loại điện, nước hoặc internet. Số tiền nguyên VND dương, tối đa 13 chữ số; có nhà cung cấp và mã đối chiếu tùy chọn. Trước khi phát hành có thể cập nhật/xóa; sau khi kỳ đã phát hành thì bị chặn. |
| Yêu cầu cư dân | Căn hộ, người báo, phân loại, tiêu đề, mô tả, trạng thái và ghi chú xử lý. Yêu cầu mới ở trạng thái Tiếp nhận; bản ghi lưu thời điểm tạo và cập nhật gần nhất. Chưa có bảng lưu từng lần chuyển trạng thái riêng. |
| Khoản thu | Mã duy nhất, tên, bắt buộc/tự nguyện, cách tính và trạng thái áp dụng. Khoản bắt buộc có lịch sử đơn giá dương với ngày hiệu lực, không trùng ngày bắt đầu trong cùng loại phí. Khoản tự nguyện không có đơn giá bắt buộc. |
| Kỳ thu | Tháng/năm duy nhất; ngày phát hành thuộc tháng của kỳ; hạn nộp cùng ngày hoặc sau ngày phát hành. |
| Bảng kê | Lưu cách tính, số lượng tính phí, đơn giá và số tiền tại thời điểm phát hành. Số đã thanh toán thay đổi khi thu tiền/cấn trừ; thông tin nhận diện căn/hộ hiện còn lấy từ hồ sơ hiện tại (R-04). |
| Công nợ | Tổng hợp khoản phải thu, đã cấn trừ và còn nợ theo hộ trong kỳ; lọc trạng thái và khoảng tầng. Diện tích, nhân khẩu và số dư nộp trước hiển thị ở đây là dữ liệu hiện tại, không phải toàn bộ snapshot lịch sử. |
| Giao dịch | Người nộp 1–50 ký tự, ngày thu không ở tương lai, số tiền nguyên VND dương, hình thức tiền mặt/chuyển khoản, mã giao dịch tùy chọn tối đa 120 ký tự, ghi chú tối đa 500 ký tự và người ghi nhận. |
| Đóng góp | Lưu từng lần nhận tiền nguyên VND dương, ngày không ở tương lai, căn hộ và loại đóng góp đang hoạt động. Không tạo khoản phải thu. |
| CSV | Xuất các dòng của bảng đang hiển thị. Tệp UTF-8 có BOM và dòng `sep=,` để hỗ trợ Excel; số tiền lưu dạng số không kèm ký hiệu tiền. Có xác nhận trước khi ghi đè. |
| Tài khoản | Tên đăng nhập 3–64 ký tự, được chuyển về chữ thường; tên hiển thị 2–120 ký tự. Mật khẩu ít nhất 8 ký tự và không quá 72 byte UTF-8; lưu bằng BCrypt. Đổi mật khẩu cần mật khẩu cũ đúng, xác nhận khớp và mật khẩu mới khác mật khẩu cũ. |

Tên ở trường `apartments.household_name` được nhập dưới nhãn Người liên hệ nhưng cũng xuất hiện dưới nhãn Chủ hộ trên các bảng tài chính. Khi lưu nhân khẩu là chủ hộ, hệ thống ghi tên này sang căn hộ; sửa căn hộ độc lập có thể làm hai nguồn tên lệch nhau. Đây là R-05, cần thống nhất mô hình và nhãn trước khi nghiệm thu.

## Tính phí

### Công thức

| Cách tính | Số tiền |
|---|---|
| Theo diện tích (`PER_M2`) | Diện tích × đơn giá |
| Theo nhân khẩu (`PER_RESIDENT`) | Số cư dân trong hộ tại lần phát hành × đơn giá |
| Theo xe máy/ô tô (`PER_MOTORCYCLE`, `PER_CAR`) | Số xe đúng loại đang hoạt động tại lần phát hành × đơn giá |
| Theo căn (`FIXED`) | Đơn giá cố định |
| Theo hóa đơn (`EXTERNAL_BILL`) | Số tiền nhập trên hóa đơn nhà cung cấp của căn/kỳ |
| Tự nguyện (`VOLUNTARY`) | Ghi số thực nhận, không phát hành công nợ |

Phí định kỳ được làm tròn đến đồng bằng `HALF_UP`. Phí theo số người/số xe có thể bằng 0 khi số lượng bằng 0; khoản 0 đồng có trạng thái Đã thanh toán. Các biến động thường trú/tạm trú/tạm vắng hiện chỉ được lưu lịch sử, chưa được dùng để cộng/trừ số người tính phí.

Khi phát hành, code chọn các căn có `active=TRUE` và các khoản thu có `active=TRUE`, `required=TRUE`. Cờ này độc lập với tình trạng căn hộ trống/đang ở/cho thuê. Hóa đơn tiện ích đã nhập cho tháng đó được đưa vào kỳ riêng; chỉ tắt phí xe/vệ sinh chưa đủ giữ fixture Sprint 1 nếu đã nhập hóa đơn tiện ích.

### Ngày chốt đơn giá — yêu cầu còn phải sửa (R-03)

Yêu cầu nghiệp vụ được giữ là: **lấy đơn giá có hiệu lực tại ngày 01 của tháng kỳ thu**, chọn bản ghi có ngày hiệu lực gần nhất nhưng không sau ngày đầu kỳ.

Code hiện tại trong `AppRepository.issuePeriod()` lại lấy mốc `issueDate` (ngày phát hành). Ví dụ phí dịch vụ 10.000 đồng/m² từ 01/01/2026, tăng lên 12.000 đồng/m² từ 15/10/2026, phát hành kỳ 10 vào 20/10:

| Trường hợp A101, 48 m² | Phí dịch vụ |
|---|---:|
| Theo yêu cầu ngày đầu kỳ | 480.000 đồng |
| Code hiện tại dùng ngày phát hành | 576.000 đồng |

Không dùng hành vi hiện tại làm kết quả đạt cho quy tắc đầu kỳ. Cần sửa truy vấn chọn giá và expected của test tích hợp cùng lúc. Fixture demo thông thường chưa thay đơn giá giữa tháng nên không thể phát hiện điểm lệch này.

### Dữ liệu được giữ sau phát hành

Cách tính, số lượng, đơn giá và số tiền của từng khoản không bị tính lại khi sửa hồ sơ hoặc thêm đơn giá. Chỉ số lượng liên quan đến cách tính của khoản đó được lưu trong `quantity_snapshot`, không phải một bản chụp đầy đủ mọi trường của hộ. Mã căn và tên hộ chưa được snapshot nên chứng từ in lại có thể đổi thông tin nhận diện theo hồ sơ mới (R-04).

## Thu tiền, công nợ và nộp dư

1. Chọn một khoản phải thu còn nợ trong Bảng kê phải thu, đăng nhập bằng tài khoản kế toán.
2. Nhập người nộp, ngày thu, số tiền nhận, hình thức và thông tin đối chiếu.
3. Số cấn vào khoản thu bằng `min(số tiền nhận, số còn nợ)`. Phần còn lại lưu thành số dư nộp trước của căn hộ.
4. Có thể thu nhiều lần cho một khoản. Khoản đã đủ tiền không nhận thêm phiếu thu; hiện chưa có màn hình nộp trước độc lập.
5. Số đã thanh toán của khoản gồm phần cấn từ phiếu thu và phần cấn từ số dư. Trạng thái lần lượt là Chưa thu, Thu một phần hoặc Đã thanh toán.
6. Số dư được tự động dùng khi phát hành một kỳ có tháng/năm sau kỳ nguồn của phiếu thu. Code xử lý theo thứ tự các khoản và các bản ghi số dư; không tự phân bổ lại sang kỳ đã phát hành trước đó nếu sau này mới thu thêm tiền dư.
7. Trên màn hình công nợ, số dư nộp trước là số còn lại hiện tại của hộ, kể cả khi đang chọn xem kỳ cũ. Cột Đã thanh toán là số cấn vào kỳ đang chọn, không phải toàn bộ tiền mặt/chuyển khoản đã nhận.
8. Phiếu thu lưu ngày thu do người dùng chọn; thời điểm `paid_at` được ghi ở đầu ngày. Không dùng giờ 00:00 này làm bằng chứng về giờ thực tế nhận tiền.

Ví dụ A102 có phí dịch vụ 525.000 đồng, kế toán nhận 600.000 đồng: cấn 525.000 đồng, lưu dư 75.000 đồng. Phần dư không tự thanh toán khoản phí quản lý cùng kỳ; nó được dùng khi phát hành kỳ sau theo điều kiện trên.

**Chống ghi trùng chưa hoàn thành (R-02):** mã giao dịch hiện là thông tin đối chiếu, chưa phải khóa duy nhất. Gửi lại cùng một khoản thu một phần có thể tạo thêm phiếu. Yêu cầu cần đạt là mỗi yêu cầu thu tiền có mã định danh duy nhất và việc gửi lại không làm tăng tổng đã thu. Mã giao dịch ngân hàng cần có quy tắc duy nhất phù hợp trước khi thêm ràng buộc.

## Quy trình Sprint 1

1. Khởi động database demo mới theo [README](../README.md), đăng nhập admin.
2. Kiểm tra căn hộ và nhân khẩu; khi cần thêm hồ sơ, dùng mã căn/mã hộ và số giấy tờ chưa có.
3. Kiểm tra Phí dịch vụ và Phí quản lý đang áp dụng; phí xe/vệ sinh tắt, không có hóa đơn tiện ích của kỳ.
4. Phát hành kỳ 10/2026 với ngày phát hành 01/10/2026, hạn nộp 15/11/2026.
5. Kiểm tra công thức, xem bảng kê căn hộ và in thông báo; đối chiếu với số liệu trong [Kế hoạch kiểm thử](QA_TEST_PLAN.md).
6. Đăng xuất, đăng nhập accountant; thử thu một phần, thu đủ, nộp dư trên các khoản khác nhau.
7. Đối chiếu công nợ, tổng quan và lịch sử. Phát hành kỳ 11/2026 bằng admin để kiểm tra cấn trừ số dư.
8. Kiểm tra quyền bị từ chối tại service theo kế hoạch QA. Việc không thấy menu chưa đủ chứng minh service đã chặn quyền.

Kịch bản trên phục vụ demo luồng đã có; không thay thế các case lỗi, migration và chốt giá giữa tháng.

## Phần mở rộng

- **Phương tiện:** admin đăng ký/ngừng đăng ký xe; bật loại phí xe khi muốn đưa vào kỳ mới. Mức khởi tạo là 70.000 đồng/xe máy và 1.200.000 đồng/ô tô.
- **Tiện ích:** admin nhập hóa đơn trước phát hành. Một căn/kỳ/loại dịch vụ có một hóa đơn; lưu lại cùng bộ khóa có thể cập nhật số tiền thay vì tạo bản ghi thứ hai.
- **Yêu cầu cư dân:** admin tiếp nhận, phân loại, cập nhật trạng thái và ghi chú; tra cứu bản ghi hoàn tất vẫn được.
- **Biến động cư trú:** admin ghi người, trạng thái, khoảng ngày và lý do; chưa tác động đến công thức phí nhân khẩu.
- **Đóng góp:** admin tạo loại tự nguyện; cả admin và accountant ghi số tiền thực nhận. Theo dõi riêng, không cộng vào công nợ bắt buộc.
- **CSV:** xuất bảng kê, lịch sử thu, tiện ích hoặc đóng góp. Giữ nguyên dữ liệu sau lọc; xem mã hóa và nội dung khi mở bằng Excel hoặc trình đọc CSV.

## Điều kiện chấp nhận

| Nhóm | Điều kiện cần kiểm tra |
|---|---|
| Hồ sơ | Mã duy nhất, dữ liệu hợp lệ; mỗi hộ có cư dân có đúng một chủ hộ; việc thay chủ hộ có xác nhận. |
| Phát hành | Mỗi tháng một kỳ; đúng khoản và đúng công thức; thiếu giá phải rollback; ngày chọn giá đáp ứng quy tắc đầu kỳ sau khi sửa R-03. |
| Chứng từ | Số tiền và thông tin hộ đúng với thời điểm cần đối soát; cần sửa/chốt chính sách R-04, R-05. Kiểm tra bản in thực tế. |
| Thu tiền | Đúng số nhận/cấn/dư, có lịch sử; gửi lại một yêu cầu không tạo phiếu mới sau khi sửa R-02. |
| Quyền và tài khoản | Service từ chối thao tác sai vai trò, khóa sau 5 lần sai và mở lại sau 5 phút; mật khẩu mới tuân thủ quy tắc. |
| Nâng cấp và giao diện | Bootstrap không phụ thuộc dữ liệu mẫu trên database mới hoặc cũ; giao diện có phản hồi khi database chậm (R-06). |

Danh sách mã vấn đề, trạng thái và phạm vi kiểm thử được theo dõi tại [Đối chiếu Sprint 1](SPRINT1_COVERAGE_REVIEW.md) và [Biên bản kiểm thử](QA_TEST_REPORT.md).
