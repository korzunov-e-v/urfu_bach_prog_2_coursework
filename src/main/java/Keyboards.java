import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

class Keyboards {
    static InlineKeyboardMarkup getMainKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Все товары");
        button1.setCallbackData("all_products");
        rowInline1.add(button1);
        rowsInline.add(rowInline1);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Все группы");
        button2.setCallbackData("all_groups");
        rowInline2.add(button2);
        rowsInline.add(rowInline2);

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Настройки");
        button3.setCallbackData("settings");
        rowInline3.add(button3);
        rowsInline.add(rowInline3);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getGroupsKeyboard(NotificationBot.State state, List<Group> groups) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInlineAddGroup = new ArrayList<>();
        InlineKeyboardButton buttonAddGroup = new InlineKeyboardButton();
        buttonAddGroup.setText("Добавить группу");
        buttonAddGroup.setCallbackData("add_group");
        rowInlineAddGroup.add(buttonAddGroup);
        rowsInline.add(rowInlineAddGroup);

        List<InlineKeyboardButton> rowInlineDeleteGroup = new ArrayList<>();
        InlineKeyboardButton buttonDeleteGroup = new InlineKeyboardButton();
        buttonDeleteGroup.setText("Удалить группу");
        buttonDeleteGroup.setCallbackData("delete_group");
        rowInlineDeleteGroup.add(buttonDeleteGroup);
        rowsInline.add(rowInlineDeleteGroup);

//        // TODO get groups
//        long userId = state.userId;
//        List<MyAmazingBot.Group> groups = new ArrayList<>();
//        groups.add(new MyAmazingBot.Group(0, "Group_1", userId));
//        groups.add(new MyAmazingBot.Group(1, "Group_2", userId));

        for (NotificationBot.Group group : groups.subList(state.page * Constants.PAGE_SIZE, (state.page+1) * 5)) {
            List<InlineKeyboardButton> rowInlineGroup = new ArrayList<>();
            InlineKeyboardButton buttonGroup = new InlineKeyboardButton();
            buttonGroup.setText(group.name);
            buttonGroup.setCallbackData("group_retrieve=" + group.id);
            rowInlineGroup.add(buttonGroup);
            rowsInline.add(rowInlineGroup);
        }

        List<InlineKeyboardButton> rowInlinePag = new ArrayList<>();
        if (state.page > 0) {
            InlineKeyboardButton buttonLeft = new InlineKeyboardButton();
            buttonLeft.setText("<");
            buttonLeft.setCallbackData("group=" + state.groupId + ";p="+(state.page-1));  // TODO change url
            rowInlinePag.add(buttonLeft);
        }
        if (groups.size() - state.page * Constants.PAGE_SIZE > 1) { // TODO: check condition
            InlineKeyboardButton buttonRight = new InlineKeyboardButton();
            buttonRight.setText(">");
            buttonRight.setCallbackData("group=" + state.groupId + ";p="+(state.page+1));  // TODO change url
            rowInlinePag.add(buttonRight);
        }
        rowsInline.add(rowInlinePag);

        List<InlineKeyboardButton> rowInlineBack = new ArrayList<>();
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("main");
        rowInlineBack.add(buttonBack);
        rowsInline.add(rowInlineBack);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getGroupRetrieveKeyboard(NotificationBot.State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInlineAddProduct = new ArrayList<>();
        InlineKeyboardButton buttonAddProduct = new InlineKeyboardButton();
        buttonAddProduct.setText("Добавить товар");
        buttonAddProduct.setCallbackData("add_product");
        rowInlineAddProduct.add(buttonAddProduct);
        rowsInline.add(rowInlineAddProduct);

        List<InlineKeyboardButton> rowInlineDeleteProduct = new ArrayList<>();
        InlineKeyboardButton buttonDeleteProduct = new InlineKeyboardButton();
        buttonDeleteProduct.setText("Удалить товар");
        buttonDeleteProduct.setCallbackData("delete_product");
        rowInlineDeleteProduct.add(buttonDeleteProduct);
        rowsInline.add(rowInlineDeleteProduct);

        List<InlineKeyboardButton> rowInlineResetProduct = new ArrayList<>();
        InlineKeyboardButton buttonResetProduct = new InlineKeyboardButton();
        buttonResetProduct.setText("Сбросить товар");
        buttonResetProduct.setCallbackData("reset_product");
        rowInlineResetProduct.add(buttonResetProduct);
        rowsInline.add(rowInlineResetProduct);

        List<InlineKeyboardButton> rowInlineBack = new ArrayList<>();
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("main");
        rowInlineBack.add(buttonBack);
        rowsInline.add(rowInlineBack);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getAllProductsKeyboard(NotificationBot.State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getAddProductsKeyboard(NotificationBot.State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getDeleteProductsKeyboard(NotificationBot.State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getResetProductsKeyboard(NotificationBot.State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getSettingsKeyboard(NotificationBot.State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getHelpKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInlineBack = new ArrayList<>();
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("main");
        rowInlineBack.add(buttonBack);
        rowsInline.add(rowInlineBack);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getCancelAddKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInlineBack = new ArrayList<>();
        InlineKeyboardButton buttonCancel = new InlineKeyboardButton();
        buttonCancel.setText("Закончить добавление товаров.");
        buttonCancel.setCallbackData("group_retrieve=" + state.groupId);
        rowInlineBack.add(buttonCancel);
        rowsInline.add(rowInlineBack);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

}
