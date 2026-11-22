# Hướng dẫn nghiệp vụ BlueMoon

Tài liệu mô tả luồng quản lý thu phí và các quy tắc đang áp dụng. Yêu cầu chưa được code đáp ứng ghi riêng để làm căn cứ sửa và kiểm thử lại.

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

Quyền được kiểm tra ở service, giao diện chỉ ẩn menu cho gọn. Phần tài khoản hiện có tạo, xem trạng thái và tự đổi mật khẩu. Chưa có thao tác quản trị để chủ động khóa/mở tài khoản hay đổi vai trò sau khi tạo; việc khóa 5 phút sau 5 lần sai mật khẩu là cơ chế tự động riêng.

## Thông tin quản lý

| Nhóm | Trường và quy tắc chính |
|---|---|
| Căn hộ/hộ | Mã căn gồm một chữ cái và 3-4 chữ số, như A101 hoặc P1002; tầng lấy từ phần số trước hai chữ số cuối, trong khoảng 1-30. Mã căn và mã hộ khẩu duy nhất, mã hộ có dạng `HK-...`. Diện tích dương, tối đa hai chữ số thập phân; ngày nhận nhà không ở tương lai. Có tình trạng đang ở/trống/cho thuê và cờ tính phí cho các kỳ mới. |
| Nhân khẩu | Họ tên 2-50 ký tự chữ và khoảng trắng; ngày sinh trước hôm nay; ngày chuyển đến không trước ngày sinh và không ở tương lai. Người từ 14 tuổi cần CCCD/CMND 9 hoặc 12 chữ số; số giấy tờ không trùng. Cư dân đầu tiên của hộ phải là chủ hộ. Thay chủ hộ cần xác nhận, chủ hộ cũ chuyển sang quan hệ Khác. |
| Phương tiện | Căn hộ đang hoạt động, loại xe và biển số. Biển số được chuẩn hóa, không trùng biển số đang hoạt động. Xe đã ngừng đăng ký vẫn giữ trong dữ liệu nhưng không tính ở lần phát hành sau. |
| Hóa đơn tiện ích | Một hóa đơn cho mỗi căn/kỳ/loại điện, nước hoặc internet. Số tiền nguyên VND dương, tối đa 13 chữ số; có nhà cung cấp và mã đối chiếu tùy chọn. Trước khi phát hành thì sửa/xóa được; phát hành rồi thì bị chặn. |
| Yêu cầu cư dân | Căn hộ, người báo, phân loại, tiêu đề, mô tả, trạng thái và ghi chú xử lý. Yêu cầu mới ở trạng thái Tiếp nhận; bản ghi lưu thời điểm tạo và lần cập nhật gần nhất. Chưa có bảng lưu riêng từng lần chuyển trạng thái. |
| Khoản thu | Mã duy nhất, tên, bắt buộc/tự nguyện, cách tính và trạng thái áp dụng. Khoản bắt buộc có lịch sử đơn giá dương kèm ngày hiệu lực, không trùng ngày bắt đầu trong cùng loại phí. Khoản tự nguyện không có đơn giá bắt buộc. |
| Kỳ thu | Tháng/năm duy nhất; ngày phát hành thuộc tháng của kỳ; hạn nộp cùng ngày hoặc sau ngày phát hành. |
| Bảng kê | Lưu cách tính, số lượng tính phí, đơn giá và số tiền tại thời điểm phát hành. Số đã thanh toán thay đổi khi thu tiền hoặc cấn trừ; thông tin nhận diện căn/hộ vẫn lấy từ hồ sơ hiện tại (R-04). |
| Công nợ | Tổng hợp khoản phải thu, đã cấn trừ và còn nợ theo hộ trong kỳ; lọc được theo trạng thái và khoảng tầng. Diện tích, nhân khẩu và số dư nộp trước hiện ở đây là dữ liệu hiện tại, không phải snapshot lịch sử. |
| Giao dịch | Người nộp 1-50 ký tự, ngày thu không ở tương lai, số tiền nguyên VND dương, hình thức tiền mặt/chuyển khoản, mã giao dịch bắt buộc tối đa 120 ký tự, ghi chú tối đa 500 ký tự và người ghi nhận. |
| Đóng góp | Lưu từng lần nhận tiền nguyên VND dương, ngày không ở tương lai, căn hộ và loại đóng góp đang hoạt động. Không tạo khoản phải thu. |
| CSV | Xuất các dòng đang hiện trên bảng. Tệp UTF-8 có BOM và dòng `sep=,` để Excel mở đúng; số tiền để dạng số, không kèm ký hiệu tiền. Ghi đè thì có xác nhận trước. |
| Tài khoản | Tên tài khoản nội bộ 3-64 ký tự, tên hiển thị 2-120 ký tự; tài khoản mới phải có email hoặc số điện thoại duy nhất để đăng nhập và khôi phục. Email chuẩn hóa chữ thường, số điện thoại Việt Nam chuẩn hóa về dạng 10 số. Mật khẩu ít nhất 8 ký tự và không quá 72 byte UTF-8, lưu bằng BCrypt. Đổi mật khẩu cần mật khẩu cũ đúng, xác nhận khớp và mật khẩu mới khác mật khẩu cũ; quên mật khẩu chỉ tra cứu được bằng email/số điện thoại đã đăng ký. |

Trường `apartments.household_name` nhập dưới nhãn Người liên hệ nhưng lại hiện dưới nhãn Chủ hộ trên các bảng tài chính. Lưu nhân khẩu là chủ hộ thì hệ thống ghi tên này sang căn hộ; sửa căn hộ riêng có thể làm hai nguồn tên lệch nhau. Đây là R-05, cần thống nhất mô hình và nhãn trước khi nghiệm thu.

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

Phí định kỳ làm tròn đến đồng bằng `HALF_UP`. Phí theo số người/số xe bằng 0 khi số lượng bằng 0; khoản 0 đồng có trạng thái Đã thanh toán. Biến động thường trú/tạm trú/tạm vắng hiện chỉ lưu lịch sử, chưa dùng để cộng/trừ số người tính phí.

Lúc phát hành, code lấy các căn có `active=TRUE` và các khoản thu có `active=TRUE`, `required=TRUE`. Cờ này độc lập với tình trạng căn hộ trống/đang ở/cho thuê. Hóa đơn tiện ích đã nhập cho tháng đó vẫn vào kỳ, kể cả khi các khoản phí định kỳ khác bị tắt.

### Ngày chốt đơn giá (R-03)

Yêu cầu nghiệp vụ: **lấy đơn giá có hiệu lực tại ngày 01 của tháng kỳ thu**, chọn bản ghi có ngày hiệu lực gần nhất nhưng không sau ngày đầu kỳ.

Mốc chọn giá nằm trong `AppRepository.issuePeriod()`. Ví dụ phí dịch vụ 10.000 đồng/m² từ 01/01/2026, tăng lên 12.000 đồng/m² từ 15/10/2026, phát hành kỳ 10 vào 20/10:

| Trường hợp A101, 48 m² | Phí dịch vụ |
|---|---:|
| Theo yêu cầu ngày đầu kỳ | 480.000 đồng |
| Nếu lấy theo ngày phát hành | 576.000 đồng |

Truy vấn hiện dùng ngày đầu kỳ. Case MySQL ngày 25/11 đã kiểm tra ngày phát hành sau ngày đổi giá; bộ dữ liệu không có thay đổi giá giữa tháng thì không phát hiện được chỗ lệch này.

### Dữ liệu giữ lại sau phát hành

Cách tính, số lượng, đơn giá và số tiền của từng khoản không bị tính lại khi sửa hồ sơ hay thêm đơn giá. Chỉ số lượng gắn với cách tính của khoản đó được lưu trong `quantity_snapshot`, không phải bản chụp đầy đủ mọi trường của hộ. Mã căn và tên hộ không nằm trong snapshot nên chứng từ in lại lấy thông tin nhận diện theo hồ sơ tại thời điểm đọc (R-04).

## Thu tiền, công nợ và nộp dư

1. Đăng nhập bằng tài khoản kế toán, chọn một khoản còn nợ trong Bảng kê phải thu.
2. Nhập người nộp, ngày thu, số tiền nhận, hình thức và thông tin đối chiếu.
3. Số cấn vào khoản thu bằng `min(số tiền nhận, số còn nợ)`. Phần còn lại lưu thành số dư nộp trước của căn hộ.
4. Một khoản thu nhiều lần được. Khoản đã đủ tiền không nhận thêm phiếu; chưa có màn hình nộp trước độc lập.
5. Số đã thanh toán của khoản gồm phần cấn từ phiếu thu và phần cấn từ số dư. Trạng thái lần lượt là Chưa thu, Thu một phần hoặc Đã thanh toán.
6. Số dư tự động được dùng khi phát hành kỳ có tháng/năm sau kỳ nguồn của phiếu thu. Thứ tự xử lý theo danh sách khoản và bản ghi số dư; số dư phát sinh sau không phân bổ ngược vào kỳ đã phát hành.
7. Trên màn hình công nợ, số dư nộp trước là số còn lại của hộ tại thời điểm xem, kể cả khi đang chọn kỳ cũ. Cột Đã thanh toán là số cấn vào kỳ đang chọn, không phải toàn bộ tiền mặt/chuyển khoản đã nhận.
8. Phiếu thu lưu ngày thu do người dùng chọn; `paid_at` ghi ở đầu ngày. Đừng lấy giờ 00:00 đó làm bằng chứng về giờ nhận tiền thực tế.

Ví dụ A102 có phí dịch vụ 525.000 đồng, kế toán nhận 600.000 đồng: cấn 525.000 đồng, lưu dư 75.000 đồng. Phần dư không tự thanh toán khoản phí quản lý cùng kỳ; nó được dùng khi phát hành kỳ sau theo điều kiện trên.

**Chống ghi trùng (R-02):** mỗi phiếu cần mã giao dịch. Màn hình tạo sẵn mã mới; với chuyển khoản, kế toán thay bằng mã ngân hàng để đối chiếu. Lúc lưu, ứng dụng dùng khóa MySQL và từ chối mã đã tồn tại, kể cả hai yêu cầu đồng thời. Thử lại một yêu cầu thì giữ nguyên mã. Hai mã khác nhau không được nhận diện là cùng giao dịch; ghi SQL trực tiếp không qua ứng dụng thì khóa này không bảo vệ.

## Quy trình sử dụng

1. Mở ứng dụng. Chưa có tài khoản thì tạo quản trị đầu tiên, có rồi thì đăng nhập.
2. Ban quản trị thêm căn hộ, cư dân và phương tiện cần quản lý.
3. Tạo khoản thu, chọn cách tính và đặt đơn giá kèm ngày hiệu lực. Không có khoản thu hay mức giá khởi tạo sẵn.
4. Nhập hóa đơn tiện ích của tháng nếu có, rồi phát hành kỳ với hạn nộp phù hợp.
5. Kiểm tra bảng kê, công thức và thông báo trước khi thu tiền.
6. Kế toán đăng nhập bằng tài khoản được quản trị tạo, ghi nhận tiền nộp cho từng khoản.
7. Đối chiếu công nợ, lịch sử thu và số dư. Số dư được xét cấn khi phát hành kỳ sau.

Các bước và số liệu kiểm tra cụ thể nằm trong [hướng dẫn QA](../qa/QA.md).

## Phần mở rộng

- **Phương tiện:** admin đăng ký hoặc ngừng đăng ký xe, bật loại phí xe khi muốn đưa vào kỳ mới. Đơn giá do ban quản trị đặt, không có mức khởi tạo sẵn.
- **Tiện ích:** admin nhập hóa đơn trước khi phát hành. Mỗi căn/kỳ/loại dịch vụ có một hóa đơn; lưu lại cùng bộ khóa sẽ cập nhật số tiền thay vì tạo bản ghi thứ hai.
- **Yêu cầu cư dân:** admin tiếp nhận, phân loại, cập nhật trạng thái và ghi chú; vẫn tra cứu được bản ghi đã hoàn tất.
- **Biến động cư trú:** admin ghi người, trạng thái, khoảng ngày và lý do; chưa tác động tới công thức phí nhân khẩu.
- **Đóng góp:** admin tạo loại tự nguyện; cả admin và accountant ghi số tiền thực nhận. Theo dõi riêng, không cộng vào công nợ bắt buộc.
- **CSV:** xuất bảng kê, lịch sử thu, tiện ích hoặc đóng góp, giữ nguyên dữ liệu sau lọc. Mở bằng Excel hay trình đọc CSV thì kiểm tra lại mã hóa và nội dung.

## Điều kiện chấp nhận

| Nhóm | Điều kiện cần kiểm tra |
|---|---|
| Hồ sơ | Mã duy nhất, dữ liệu hợp lệ; mỗi hộ có cư dân thì đúng một chủ hộ; thay chủ hộ có xác nhận. |
| Phát hành | Mỗi tháng một kỳ; đúng khoản và đúng công thức; thiếu giá thì rollback; ngày chọn giá theo đầu kỳ. |
| Chứng từ | Số tiền và thông tin hộ đúng với thời điểm cần đối soát; cần sửa hoặc chốt chính sách R-04, R-05. Kiểm tra bản in thật. |
| Thu tiền | Đúng số nhận/cấn/dư, có lịch sử; mã giao dịch đã tồn tại bị từ chối. |
| Quyền và tài khoản | Service từ chối thao tác sai vai trò; khóa sau 5 lần sai, mở lại sau 5 phút; mật khẩu mới đúng quy tắc. |
| Nâng cấp và giao diện | Bootstrap không phụ thuộc dữ liệu mẫu trên database mới hay cũ; giao diện có phản hồi khi database chậm (R-06). |

Danh sách mã vấn đề, trạng thái và phạm vi kiểm thử theo dõi tại [Đối chiếu Sprint 1](../qa/SPRINT1_COVERAGE_REVIEW.md) và [Biên bản kiểm thử](../qa/QA_TEST_REPORT.md).
