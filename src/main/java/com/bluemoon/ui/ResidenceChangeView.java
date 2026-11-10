package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.ResidenceChange;
import com.bluemoon.model.Domain.ResidenceStatus;
import com.bluemoon.model.Domain.ResidentDirectoryEntry;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

final class ResidenceChangeView extends VBox {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final AppServices services;
    private final AppUser user;
    private final TableView<ResidenceChange> table = new TableView<>();

    ResidenceChangeView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        Button refresh = Ui.button("Làm mới", false);
        refresh.setOnAction(event -> load());
        Button add = Ui.button("Ghi nhận biến động", true);
        add.setOnAction(event -> addChange());
        getChildren().addAll(Ui.pageHeader("Biến động cư trú", ""),
                Ui.toolbar(refresh, add), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setPlaceholder(new Label("Chưa có biến động cư trú."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Nhân khẩu", ResidenceChange::residentName));
        table.getColumns().add(column("Căn hộ", ResidenceChange::apartmentCode));
        table.getColumns().add(column("Trạng thái", change -> statusName(change.status())));
        table.getColumns().add(column("Từ ngày", change -> DATE.format(change.startDate())));
        table.getColumns().add(column("Đến ngày", change -> change.endDate() == null ? "Đang hiệu lực" : DATE.format(change.endDate())));
        table.getColumns().add(column("Lý do", ResidenceChange::reason));
        table.getColumns().add(column("Người ghi nhận", ResidenceChange::recordedBy));
    }

    private void load() {
        try {
            table.getItems().setAll(services.residenceChanges().list(user));
        } catch (SQLException exception) {
            Ui.error("Không tải được biến động cư trú", exception);
        }
    }

    private void addChange() {
        List<ResidentDirectoryEntry> entries;
        try {
            entries = services.residents().search(user, "").stream().filter(entry -> entry.resident() != null).toList();
        } catch (SQLException exception) {
            Ui.error("Không tải được danh sách nhân khẩu", exception);
            return;
        }
        if (entries.isEmpty()) {
            Ui.info("Chưa có nhân khẩu", "Hãy thêm nhân khẩu trước khi ghi nhận biến động.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ghi nhận biến động cư trú");
        dialog.setHeaderText("Cập nhật trạng thái cư trú");
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Lưu biến động", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);

        ComboBox<ResidentDirectoryEntry> resident = new ComboBox<>();
        resident.getItems().setAll(entries);
        resident.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(ResidentDirectoryEntry value) {
                return value == null ? "" : value.resident().fullName() + " · " + value.apartment().code();
            }
            @Override public ResidentDirectoryEntry fromString(String value) { return null; }
        });
        resident.setValue(entries.getFirst());
        resident.getStyleClass().add("form-field");
        ComboBox<ResidenceStatus> status = new ComboBox<>();
        status.getItems().setAll(ResidenceStatus.values());
        status.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(ResidenceStatus value) { return value == null ? "" : statusName(value); }
            @Override public ResidenceStatus fromString(String value) { return null; }
        });
        status.setValue(ResidenceStatus.TEMPORARY_RESIDENCE);
        status.getStyleClass().add("form-field");
        DatePicker start = new DatePicker(LocalDate.now());
        start.getStyleClass().add("form-field");
        DatePicker end = new DatePicker();
        end.getStyleClass().add("form-field");
        TextArea reason = new TextArea();
        reason.setPromptText("Ví dụ: đi công tác, chuyển đến tạm thời...");
        reason.setWrapText(true);
        reason.setPrefRowCount(3);
        reason.getStyleClass().add("form-field");
        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(12); form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Nhân khẩu", resident); addRow(form, 1, "Trạng thái", status);
        addRow(form, 2, "Từ ngày", start); addRow(form, 3, "Đến ngày", end); addRow(form, 4, "Lý do", reason);
        dialog.getDialogPane().setContent(form); dialog.getDialogPane().setMinWidth(590);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                if (resident.getValue() == null || resident.getValue().resident() == null) throw new IllegalArgumentException("Chọn nhân khẩu.");
                services.residenceChanges().record(user, resident.getValue().resident().id(), status.getValue(),
                        start.getValue(), end.getValue(), reason.getText());
            } catch (Exception exception) {
                event.consume(); Ui.error("Không lưu được biến động cư trú", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private static String statusName(ResidenceStatus status) {
        return switch (status) {
            case PERMANENT_RESIDENCE -> "Thường trú";
            case TEMPORARY_RESIDENCE -> "Tạm trú";
            case TEMPORARY_ABSENCE -> "Tạm vắng";
        };
    }

    private static void addRow(GridPane form, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title); label.getStyleClass().add("form-label"); label.setLabelFor(field);
        form.add(label, 0, row); form.add(field, 1, row); GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static <T> TableColumn<ResidenceChange, String> column(String title, Function<ResidenceChange, String> value) {
        TableColumn<ResidenceChange, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
