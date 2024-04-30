package bot;

import database.models.Group;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static bot.NotificationBot.State;
import static bot.NotificationBot.ProductCreationStatus;
import static bot.Queries.getGroups;

public class Messaging {

    static SendMessage getMessageStart(Update update) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Привет. Это стартовое сообщение"); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageMainMenu(Update update) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getFrom().getId());
        message.setText("Это главное меню"); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageHelp(Update update) {
        InlineKeyboardMarkup kbm = Keyboards.getCancelKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Привет. Это справка."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageNotKnownCommand(Update update) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getFrom().getId());
        message.setText("Бот такой команды не знает =(.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAllGroups(Update update, State state) {
        InlineKeyboardMarkup kbm = Keyboards.getGroupsKeyboard(state);
        SendMessage message = new SendMessage();
        long userId = update.getMessage().getFrom().getId();
        message.setChatId(userId);
        List<Group> groups = getGroups(userId);
        message.setText("Привет. Это группы. \n" + groups); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddProductSuccess(Update update, State state, ProductCreationStatus status) {
        InlineKeyboardMarkup kbm = bot.Keyboards.getCancelCreateProductKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getFrom().getId());
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

}
