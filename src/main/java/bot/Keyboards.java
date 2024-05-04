package bot;

import database.models.Group;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static bot.NotificationBot.State;
import static bot.NotificationBot.Menu;

// TODO
class Keyboards {
    static InlineKeyboardMarkup getMainKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Все товары");
        button1.setCallbackData(Menu.ALL_PRODUCTS.toString());
        rowInline1.add(button1);
        rowsInline.add(rowInline1);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Все группы");
        button2.setCallbackData(Menu.ALL_GROUPS.toString());
        rowInline2.add(button2);
        rowsInline.add(rowInline2);

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Настройки");
        button3.setCallbackData(Menu.SETTINGS.toString());
        rowInline3.add(button3);
        rowsInline.add(rowInline3);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getAllGroupsKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInlineAddGroup = new ArrayList<>();
        InlineKeyboardButton buttonAddGroup = new InlineKeyboardButton();
        buttonAddGroup.setText("Добавить группу");
        buttonAddGroup.setCallbackData(Menu.ADD_GROUPS.toString());
        rowInlineAddGroup.add(buttonAddGroup);
        rowsInline.add(rowInlineAddGroup);

        List<InlineKeyboardButton> rowInlineDeleteGroup = new ArrayList<>();
        InlineKeyboardButton buttonDeleteGroup = new InlineKeyboardButton();
        buttonDeleteGroup.setText("Удалить группу");
        buttonDeleteGroup.setCallbackData(Menu.DELETE_GROUPS.toString());
        rowInlineDeleteGroup.add(buttonDeleteGroup);
        rowsInline.add(rowInlineDeleteGroup);
//
////        // TODO get groups
////        long userId = state.userId;
////        List<MyAmazingBot.Group> groups = new ArrayList<>();
////        groups.add(new MyAmazingBot.Group(0, "Group_1", userId));
////        groups.add(new MyAmazingBot.Group(1, "Group_2", userId));
//
//        for (bot.NotificationBot.Group group : groups.subList(state.page * bot.Constants.PAGE_SIZE, (state.page+1) * 5)) {
//            List<InlineKeyboardButton> rowInlineGroup = new ArrayList<>();
//            InlineKeyboardButton buttonGroup = new InlineKeyboardButton();
//            buttonGroup.setText(group.name);
//            buttonGroup.setCallbackData("group_retrieve=" + group.id);
//            rowInlineGroup.add(buttonGroup);
//            rowsInline.add(rowInlineGroup);
//        }
//
//        List<InlineKeyboardButton> rowInlinePag = new ArrayList<>();
//        if (state.page > 0) {
//            InlineKeyboardButton buttonLeft = new InlineKeyboardButton();
//            buttonLeft.setText("<");
//            buttonLeft.setCallbackData("group=" + state.groupId + ";p="+(state.page-1));  // TODO change url
//            rowInlinePag.add(buttonLeft);
//        }
//        if (groups.size() - state.page * bot.Constants.PAGE_SIZE > 1) {  // TODO: check condition
//            InlineKeyboardButton buttonRight = new InlineKeyboardButton();
//            buttonRight.setText(">");
//            buttonRight.setCallbackData("group=" + state.groupId + ";p="+(state.page+1));  // TODO change url
//            rowInlinePag.add(buttonRight);
//        }
//        rowsInline.add(rowInlinePag);

        List<InlineKeyboardButton> rowInlineBack = new ArrayList<>();
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData(Menu.MAIN.toString());
        rowInlineBack.add(buttonBack);
        rowsInline.add(rowInlineBack);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getAddGroupKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInlineCancel = new ArrayList<>();
        InlineKeyboardButton buttonCancel = new InlineKeyboardButton();
        buttonCancel.setText("Завершить");
        buttonCancel.setCallbackData(Menu.ALL_GROUPS.toString());
        rowInlineCancel.add(buttonCancel);
        rowsInline.add(rowInlineCancel);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getDeleteGroupsKeyboard(State state, List<Group> groups) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // TODO: pagination
        for (Group group : groups) {
            List<InlineKeyboardButton> rowInlineGroup = new ArrayList<>();
            InlineKeyboardButton buttonGroup = new InlineKeyboardButton();
            buttonGroup.setText(group.getName());
            buttonGroup.setCallbackData(Menu.DELETE_GROUP.toString() + "+" + group.getId());
            rowInlineGroup.add(buttonGroup);
            rowsInline.add(rowInlineGroup);
        }

        List<InlineKeyboardButton> rowInlineCancel = new ArrayList<>();
        InlineKeyboardButton buttonCancel = new InlineKeyboardButton();
        buttonCancel.setText("Завершить");
        buttonCancel.setCallbackData(Menu.ALL_GROUPS.toString());
        rowInlineCancel.add(buttonCancel);
        rowsInline.add(rowInlineCancel);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getRetrieveGroupKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInlineAddProduct = new ArrayList<>();
        InlineKeyboardButton buttonAddProduct = new InlineKeyboardButton();
        buttonAddProduct.setText("Добавить товар");
        buttonAddProduct.setCallbackData(Menu.ADD_PRODUCTS.toString());
        rowInlineAddProduct.add(buttonAddProduct);
        rowsInline.add(rowInlineAddProduct);

        List<InlineKeyboardButton> rowInlineDeleteProduct = new ArrayList<>();
        InlineKeyboardButton buttonDeleteProduct = new InlineKeyboardButton();
        buttonDeleteProduct.setText("Удалить товар");
        buttonDeleteProduct.setCallbackData(Menu.DELETE_PRODUCTS.toString());
        rowInlineDeleteProduct.add(buttonDeleteProduct);
        rowsInline.add(rowInlineDeleteProduct);

        List<InlineKeyboardButton> rowInlineResetProduct = new ArrayList<>();
        InlineKeyboardButton buttonResetProduct = new InlineKeyboardButton();
        buttonResetProduct.setText("Сбросить товар");
        buttonResetProduct.setCallbackData(Menu.RESET_PRODUCTS.toString());
        rowInlineResetProduct.add(buttonResetProduct);
        rowsInline.add(rowInlineResetProduct);

        List<InlineKeyboardButton> rowInlineBack = new ArrayList<>();
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData(Menu.MAIN.toString());
        rowInlineBack.add(buttonBack);
        rowsInline.add(rowInlineBack);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getAllProductsKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getAddProductsKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getDeleteProductsKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getRetrieveProductsKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getResetProductsKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getCancelCreateProductKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInlineBack = new ArrayList<>();
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Завершить");
        String command = NotificationBot.Menu.RETRIEVE_GROUP.toString();
        String arg = String.valueOf(state.groupId);
        buttonBack.setCallbackData(command + "+" + arg);
        rowInlineBack.add(buttonBack);
        rowsInline.add(rowInlineBack);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getCancelCreateGroupKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInlineBack = new ArrayList<>();
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Завершить");
        String command = NotificationBot.Menu.ALL_GROUPS.toString();
        buttonBack.setCallbackData(command);
        rowInlineBack.add(buttonBack);
        rowsInline.add(rowInlineBack);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getSettingsKeyboard(State state) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        // TODO

        List<InlineKeyboardButton> rowInlineCancel = new ArrayList<>();
        InlineKeyboardButton buttonCancel = new InlineKeyboardButton();
        buttonCancel.setText("Назад");
        buttonCancel.setCallbackData(Menu.MAIN.toString());
        rowInlineCancel.add(buttonCancel);
        rowsInline.add(rowInlineCancel);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    static InlineKeyboardMarkup getCancelKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInlineBack = new ArrayList<>();
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData(Menu.MAIN.toString());
        rowInlineBack.add(buttonBack);
        rowsInline.add(rowInlineBack);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
