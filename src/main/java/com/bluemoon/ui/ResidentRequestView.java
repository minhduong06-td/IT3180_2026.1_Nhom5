package com.bluemoon.ui;

import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.ResidentRequest;
import com.bluemoon.model.Domain.ResidentRequestInput;
import com.bluemoon.model.Domain.RequestCategory;
import com.bluemoon.model.Domain.RequestStatus;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

final class ResidentRequestView extends VBox {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AppServices services;
    private final AppUser user;
    private final TextField search = Ui.textField("Tìm căn hộ, người báo hoặc nội dung yêu cầu");
    private final ComboBox<RequestStatus> statusFilter = new ComboBox<>();
    private final TableView<ResidentRequest> table = new TableView<>();

    ResidentRequestView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        statusFilter.setPromptText("Tất cả trạng thái");
        statusFilter.getItems().setAll(RequestStatus.values());
        statusFilter.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(RequestStatus value) { return value == null ? "" : statusName(value); }
            @Override public RequestStatus fromString(String value) { return null; }
        });
        statusFilter.getStyleClass().add("form-field");
        statusFilter.setOnAction(event -> load());
        search.setOnAction(event -> load());
        Button find = Ui.button("Tìm kiếm", true);
        find.setOnAction(event -> load());
        Button clear = Ui.button("Xóa lọc", false);
        clear.setOnAction(event -> {
            search.clear();
            statusFilter.getSelectionModel().clearSelection();
            load();
        });
        Button refresh = Ui.button("Làm mới", false);
        refresh.setOnAction(event -> load());
        Button edit = Ui.button("Cập nhật yêu cầu", false);
        edit.setDisable(true);
        edit.setOnAction(event -> editSelected());
        Button add = Ui.button("Tiếp nhận yêu cầu", true);
        add.setOnAction(event -> edit(null));
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, value) ->
                edit.setDisable(value == null));
        getChildren().addAll(Ui.pageHeader("Yêu cầu cư dân",
                        "Tiếp nhận và theo dõi phản ánh, sửa chữa, vệ sinh và an ninh."),
                Ui.toolbar(search, find, clear, statusFilter, refresh, Ui.spacer(), edit, add), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setPlaceholder(new Label("Chưa có yêu cầu phù hợp."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Tiếp nhận", request -> dateTime(request.createdAt())));
        table.getColumns().add(column("Căn hộ", ResidentRequest::apartmentCode));
        table.getColumns().add(column("Người báo", ResidentRequest::reporterName));
        table.getColumns().add(column("Nội dung", ResidentRequest::subject));
        table.getColumns().add(column("Nhóm", request -> categoryName(request.category())));
        table.getColumns().add(column("Trạng thái", request -> statusName(request.status())));
        table.getColumns().add(column("Cập nhật gần nhất", request -> dateTime(request.updatedAt())));
        table.setRowFactory(view -> {
            var row = new javafx.scene.control.TableRow<ResidentRequest>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) edit(row.getItem());
            });
            return row;
        });
    }

    private void load() {
        try {
            String term = search.getText() == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
            RequestStatus selectedStatus = statusFilter.getValue();
            table.getItems().setAll(services.residentRequests().list(user).stream()
                    .filter(request -> selectedStatus == null || request.status() == selectedStatus)
                    .filter(request -> term.isEmpty() || request.apartmentCode().toLowerCase(Locale.ROOT).contains(term)
                            || request.reporterName().toLowerCase(Locale.ROOT).contains(term)
                            || request.subject().toLowerCase(Locale.ROOT).contains(term)
                            || request.description().toLowerCase(Locale.ROOT).contains(term))
                    .toList());
        } catch (SQLException exception) {
            Ui.error("Không tải được yêu cầu cư dân", exception);
        }
    }

    private void editSelected() {
        ResidentRequest selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) edit(selected);
    }

    private void edit(ResidentRequest current) {
        List<Apartment> apartments;
        try {
            apartments = services.apartments().list(user).stream()
                    .filter(apartment -> apartment.active()
                            || current != null && apartment.id().equals(current.apartmentId()))
                    .toList();
        } catch (SQLException exception) {
            Ui.error("Không tải được danh sách căn hộ", exception);
            return;
        }
        if (apartments.isEmpty()) {
            Ui.info("Chưa có căn hộ hoạt động", "Thêm hoặc kích hoạt căn hộ trước khi tiếp nhận yêu cầu.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(current == null ? "Tiếp nhận yêu cầu" : "Cập nhật yêu cầu");
        dialog.setHeaderText("Thông tin phản ánh của cư dân");
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Lưu yêu cầu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);

        ComboBox<Apartment> apartment = new ComboBox<>();
        apartment.getItems().setAll(apartments);
        apartment.setValue(current == null ? apartments.getFirst()
                : apartments.stream().filter(item -> item.id() == current.apartmentId()).findFirst().orElse(null));
        apartment.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Apartment value) {
                return value == null ? "" : value.code() + " · " + value.householdName();
            }
            @Override public Apartment fromString(String value) { return null; }
        });
        apartment.getStyleClass().add("form-field");
        TextField reporter = Ui.textField("Tên cư dân hoặc người liên hệ");
        ComboBox<RequestCategory> category = new ComboBox<>();
        category.getItems().setAll(RequestCategory.values());
        category.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(RequestCategory value) { return value == null ? "" : categoryName(value); }
            @Override public RequestCategory fromString(String value) { return null; }
        });
        category.getStyleClass().add("form-field");
        TextField subject = Ui.textField("Tóm tắt yêu cầu");
        TextArea description = new TextArea();
        description.setPromptText("Mô tả chi tiết vấn đề cư dân gặp phải");
        description.setWrapText(true);
        description.setPrefRowCount(3);
        description.getStyleClass().add("form-field");
        ComboBox<RequestStatus> status = new ComboBox<>();
        status.getItems().setAll(RequestStatus.values());
        status.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(RequestStatus value) { return value == null ? "" : statusName(value); }
            @Override public RequestStatus fromString(String value) { return null; }
        });
        status.getStyleClass().add("form-field");
        TextArea handlingNote = new TextArea();
        handlingNote.setPromptText("Cách xử lý hoặc kết quả trao đổi");
        handlingNote.setWrapText(true);
        handlingNote.setPrefRowCount(2);
        handlingNote.getStyleClass().add("form-field");
        if (current == null) {
            category.setValue(RequestCategory.MAINTENANCE);
            status.setValue(RequestStatus.RECEIVED);
            status.setDisable(true);
        } else {
            apartment.setValue(apartments.stream().filter(item -> item.id() == current.apartmentId()).findFirst().orElse(null));
            apartment.setDisable(true);
            reporter.setText(current.reporterName());
            category.setValue(current.category());
            subject.setText(current.subject());
            description.setText(current.description());
            status.setValue(current.status());
            handlingNote.setText(current.handlingNote() == null ? "" : current.handlingNote());
        }

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Căn hộ", apartment);
        addRow(form, 1, "Người báo", reporter);
        addRow(form, 2, "Nhóm yêu cầu", category);
        addRow(form, 3, "Tiêu đề", subject);
        addRow(form, 4, "Mô tả", description);
        addRow(form, 5, "Trạng thái", status);
        addRow(form, 6, "Ghi chú xử lý", handlingNote);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(560);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                Apartment selectedApartment = apartment.getValue();
                services.residentRequests().save(user, new ResidentRequestInput(current == null ? null : current.id(),
                        selectedApartment == null ? 0 : selectedApartment.id(), reporter.getText(), category.getValue(),
                        subject.getText(), description.getText(), status.getValue(), handlingNote.getText()));
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không lưu được yêu cầu cư dân", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private static String dateTime(LocalDateTime value) {
        return value == null ? "—" : DATE_TIME.format(value);
    }

    private static String categoryName(RequestCategory category) {
        return switch (category) {
            case MAINTENANCE -> "Sửa chữa";
            case CLEANING -> "Vệ sinh";
            case SECURITY -> "An ninh";
            case NOISE -> "Tiếng ồn";
            case OTHER -> "Khác";
        };
    }

    private static String statusName(RequestStatus status) {
        return switch (status) {
            case RECEIVED -> "Mới tiếp nhận";
            case IN_PROGRESS -> "Đang xử lý";
            case RESOLVED -> "Đã hoàn tất";
        };
    }

    private static void addRow(GridPane grid, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static TableColumn<ResidentRequest, String> column(String title,
                                                               Function<ResidentRequest, String> value) {
        TableColumn<ResidentRequest, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
