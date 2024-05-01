package bot;

import database.models.Group;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static bot.NotificationBot.State;
import static bot.NotificationBot.ProductCreationStatus;
import static bot.Queries.getGroups;

public class Messaging {

    static SendMessage getMessageStart(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это стартовое сообщение"); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageMainMenu(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Это главное меню"); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageSettings(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getSettingsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Это настройки"); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageHelp(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getCancelKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это справка."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageNotKnownCommand(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Бот такой команды не знает =(.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAllGroups(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getAllGroupsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        List<Group> groups = getGroups(state.userId);
        message.setText("Привет. Это группы. \n" + groups); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddGroup(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getAddGroupKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это меню добавления группы."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageDeleteGroups(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getDeleteGroupsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это меню удаления группы."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageRetrieveGroup(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getRetrieveGroupKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это меню информации о группе."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddProducts(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getAddProductsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это меню добавления товара."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageDeleteProducts(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getDeleteProductsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это меню удаления товара."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageRetrieveProduct(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getRetrieveProductsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это меню информации о товаре."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageResetProduct(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getResetProductsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это меню сброса статистики о товаре."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAllProducts(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getAllProductsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Привет. Это меню список всех товаров."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddProductSuccess(State state, ProductCreationStatus status) {
        InlineKeyboardMarkup kbm = bot.Keyboards.getCancelCreateProductKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        switch (status) {
            case SUCCESS -> message.setText("Продукт успешно добавлен."); // todo: write prod message
            case UNEXPECTED_MARKET ->
                    message.setText("Такой магазин не поддерживается. Администратор добавит возможность отслеживать цены в данном магазине в близжайшее время.");
            case UNEXPECTED_URL -> message.setText("Данная ссылка ведёт не на страницу товара.");
            case NO_PRODUCT -> message.setText("Данная страница не содержит товара. Возможно такого товара нет.");
        }
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddProductUnexpected(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getAddProductsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userId);
        message.setText("Такого сообщения не ожидалось.");
        message.setReplyMarkup(kbm);
        return message;
    }

}
