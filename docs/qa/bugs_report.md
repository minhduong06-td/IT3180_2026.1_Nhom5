# Báo cáo lỗi

Danh sách lỗi đã phát hiện, bản sửa tương ứng và các điểm thiết kế còn mở của Sprint 1, để tra cứu nhanh. Kết quả từng lượt chạy, môi trường và bằng chứng vẫn nằm trong [biên bản kiểm thử](QA_TEST_REPORT.md).

## Nguồn dữ liệu

- Lỗi lấy từ các đợt kiểm thử trong [QA_TEST_REPORT.md](QA_TEST_REPORT.md), đối chiếu yêu cầu trong [SPRINT1_COVERAGE_REVIEW.md](SPRINT1_COVERAGE_REVIEW.md).
- Bước tái hiện nằm trong [test case chức năng](QA_TEST_CASE_FUNCTIONAL.md), [test case giao diện](QA_TEST_CASE_UI.md) và [test case tích hợp](QA_TEST_CASE_INTEGRATION.md).
- `DEF-nn` là lỗi ghi nhận trong một đợt kiểm thử. `R-nn` là điểm thiết kế hoặc yêu cầu còn mở, không phải lỗi tái hiện đơn lẻ.
- Danh sách cần chạy lại sau mỗi bản sửa nằm trong [checklist hồi quy](QA_REGRESSION_CHECKLIST.md).

## Lỗi đã phát hiện và xử lý

| Mã | Mức | Hiện tượng | Phát hiện | Bản sửa | Xác nhận |
|---|---|---|---|---|---|
| DEF-01 | Trung bình | Ứng dụng kết nối nhầm cơ sở dữ liệu ngoài localhost | Đợt 1 | `0e14f83` giữ cấu hình database trên localhost | Đợt 4 chạy lại không tái hiện |
| DEF-02 | Trung bình | Phụ đề lặp lại tiêu đề trang, chiếm chỗ trên cửa sổ nhỏ | Đợt 2 | `0fb7171` rút gọn phần tiêu đề và bố cục | Đợt 2 kiểm tra lại đạt, đợt 4 đạt |
| DEF-03 | Thấp | Bảng rỗng hiện thông báo mặc định bằng tiếng Anh | Đợt 2 | Sửa nhãn trạng thái rỗng trong giao diện | Đợt 2 kiểm tra lại đạt |
| DEF-04 | Cao | Nâng cấp cơ sở dữ liệu đã có dữ liệu bị lỗi, ứng dụng không khởi động | Đợt 3 | `af0f8d4` sửa migration thanh toán và bổ sung test | INT-07 đạt ở đợt 3 và đợt 4 |
| DEF-05 | Cao | Tiền nộp dư ở kỳ sau bị cấn ngược vào kỳ đã phát hành trước đó | Đợt 3 | `3cd1c40` chỉ cấn số dư vào kỳ sau kỳ phát sinh | INT-03 và INT-04 đạt ở đợt 3 và đợt 4 |

## Điểm còn mở

| Mã | Mức | Nội dung | Trạng thái | Việc cần làm |
|---|---|---|---|---|
| R-02 | Cao | Mã giao dịch bắt buộc; repository kiểm tra mã đã tồn tại trong transaction và dùng advisory lock MySQL | Đã sửa và có test; còn giới hạn | Bổ sung ở `51b8448` và `26730f0`. Hai mã khác nhau cho cùng một lần nộp, hoặc ghi SQL trực tiếp, thì không phát hiện được. Tiếp tục hồi quy theo FUN-24 và FUN-30. |
| R-03 | Cao | Đơn giá phải chọn theo ngày đầu kỳ thu, không theo ngày phát hành | Đã sửa và có test | `BillingRules.rateDate()` và `7c829fa` dùng ngày đầu tháng; test `2f2539b` và ca FUN-29 kiểm tra đổi giá giữa tháng. Hồi quy lại khi sửa công thức tính phí. |
| R-04 | Trung bình | Chứng từ in lại lấy mã căn và tên hộ theo hồ sơ hiện tại | Còn mở | Chốt thông tin nhận diện nào phải giữ nguyên tại thời điểm phát hành, bổ sung snapshot và xác nhận bằng FUN-28. |
| R-05 | Trung bình | Tên người liên hệ của căn hộ và tên chủ hộ có thể lệch nhau | Còn mở | Chốt một nguồn tên chủ hộ hoặc tách hai trường, sửa nhãn trên màn hình, công nợ và chứng từ. |
| R-06 | Trung bình | Nhiều thao tác đọc ghi cơ sở dữ liệu chạy trên luồng giao diện | Chưa đủ kết quả để kết luận | Đo thời gian phản hồi khi database chậm, thử thao tác lặp và in cả kỳ; ghi lại cách tái hiện. |

## Trạng thái kiểm thử gần nhất

- `mvnw.cmd test` trên bản hiện tại: 30 test, 28 đạt, 0 lỗi, 2 bỏ qua; hai test bỏ qua là test tích hợp MySQL chưa cấu hình database kiểm thử.
- Test hồi quy MySQL cho R-02 và R-03 chỉ chạy khi bật `BLUEMOON_IT_EXISTING_DB`; điều kiện và lệnh chạy nằm trong [QA.md](QA.md).
- Chưa xác nhận trên bản in PDF thực tế và chưa có case đo độ trễ cho R-06.
- Test bị bỏ qua ghi là Chưa chạy, không tính là đạt khi kết luận nghiệm thu.

## Quy ước trạng thái

| Trạng thái | Ý nghĩa |
|---|---|
| Mới | Đã ghi nhận, chưa có bản sửa. |
| Đang sửa | Có người nhận và đang xử lý. |
| Đã sửa, chờ kiểm tra | Có commit sửa, chưa chạy lại case liên quan. |
| Đã đóng | Có commit sửa, case hồi quy đạt và bằng chứng trong biên bản. |
| Chấp nhận có giới hạn | PM quyết định chấp nhận, ghi rõ điều kiện và rủi ro còn lại. |

Đóng một lỗi cần đủ ba phần: commit hoặc bản dựng chứa bản sửa, case hồi quy tương ứng đạt, và bằng chứng lưu trong [biên bản kiểm thử](QA_TEST_REPORT.md). Lỗi chưa có case hồi quy thì coi như chưa đóng.
