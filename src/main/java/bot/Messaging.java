package bot;

import database.HibernateUtil;
import database.models.Group;
import database.models.Product;
import mongo.MongoUtil;
import org.hibernate.SessionFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static bot.NotificationBot.ProductCreationStatus;
import static bot.NotificationBot.GroupCreationStatus;
import static bot.NotificationBot.DeletionStatus;
import static bot.NotificationBot.State;

public class Messaging {

    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    static SendMessage getMessageStart(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Привет. Это главное меню. Чтобы добавить товар в отслеживаемые, создай группу товаров и добавь туда интересующие товары.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageMainMenu(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Главное меню.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageSettings(State state, boolean notif) {
        InlineKeyboardMarkup kbm = Keyboards.getSettingsKeyboard(state, notif);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Настройки.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageHelp(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getCancelKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Справка.\n\nЧтобы добавить товар, можно создать группу и после добавить туда товары. Бот будет следить за ценой и уведомлять об изменении.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageNotKnownCommand(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Бот такой команды не знает =(.");
        message.setReplyMarkup(kbm);
        return message;
    }

    // todo
    static SendMessage getMessageAllGroups(State state, List<Group> groups) {
        InlineKeyboardMarkup kbm = Keyboards.getAllGroupsKeyboard(state, groups);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);

        StringBuilder sb = new StringBuilder("Список групп. \n\n");
        for (Group group : groups) {
            List<Product> products = group.getProducts();

            Double minPrice = null;
            Double maxPrice = null;
            Double curr = 0.0;
            for (Product product : products) {
                curr = MongoUtil.getCurrentPrice(product.getId());
                if (curr != null) {
                    if (minPrice == null) {
                        minPrice = curr;
                        maxPrice = curr;
                    }
                    minPrice = Math.min(minPrice, curr);
                    maxPrice = Math.max(maxPrice, curr);
                }
            }

            if (minPrice == null) {
                minPrice = (double) 0;
                maxPrice = (double) 0;
            }
            sb.append(String.format("- %s - от %d до %d руб.\n", group.getName(), minPrice.intValue(), maxPrice.intValue()));
//            sb.append(String.format("  +%d руб за месяц\n", 0));  // todo
//            sb.append(String.format("  +%d руб с момента добавления\n", 0));  // todo
            sb.append("\n");
        }

        message.setText(sb.toString());
        message.enableMarkdown(true);
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddGroups(State state, List<Group> groups) {
        InlineKeyboardMarkup kbm = Keyboards.getAddGroupKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);

        StringBuilder sb = new StringBuilder("Меню добавления группы. Чтобы создать группу, можно отпрвить имя новой группы в следующем сообщении.\n\n");
        for (Group group : groups) {
            sb.append("- ");
            sb.append(group.getName());
            sb.append("\n");
        }

        message.setText(sb.toString());
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddGroupSuccess(State state, GroupCreationStatus status) {
        InlineKeyboardMarkup kbm = bot.Keyboards.getCancelCreateGroupKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        switch (status) {
            case SUCCESS ->
                    message.setText("Группа успешно добавлена.");
            case ALREADY_EXISTS ->
                    message.setText("Группа не добавлена, группа с таким именем уже существует.");
        }
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageDeleteGroups(State state, List<Group> groups) {
        InlineKeyboardMarkup kbm = Keyboards.getDeleteGroupsKeyboard(state, groups);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Меню удаления группы. После нажатия на название группы, она будет удалена, вместе с содержащимися товарами.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageDeleteGroupSuccess(State state, DeletionStatus status,
            String groupName) {
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        switch (status) { // todo: write prod messages
            case SUCCESS -> message.setText("Группа '" + groupName + "' успешно удалена.");
            case NOT_FOUND -> message.setText("Группа не удалена (не найдена)");
            case FORBIDDEN -> message.setText("Группа не удалена (нет доступа)");
        }
        return message;
    }

    static SendMessage getMessageRetrieveGroup(State state, Group group, List<Product> products) {
        InlineKeyboardMarkup kbm = Keyboards.getRetrieveGroupKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);


        StringBuilder sb = new StringBuilder(
                String.format("Меню информации о группе '%s' \n\n", group.getName()));
        if (products.size() == 0) {
            sb.append("Пока что тут нет товаров.");
        } else {
            for (Product product : products) {
                Double currentPrice = null;
                Double minPrice = null;
                Double maxPrice = null;

                long productId = product.getId();
                currentPrice = MongoUtil.getCurrentPrice(productId);
                if (currentPrice == null) {
                    currentPrice = (double) 0;
                    minPrice = (double) 0;
                    maxPrice = (double) 0;
                } else {
                    minPrice = MongoUtil.getMinPrice(productId);
                    maxPrice = MongoUtil.getMaxPrice(productId);
                }

                assert minPrice != null;
                assert maxPrice != null;
                sb.append(String.format(
                        "- %s - %.2f руб (изм от %d до %d руб).\n", product.getName(), currentPrice, minPrice.intValue(), maxPrice.intValue()));
                sb.append("\n");
            }
        }

        message.setText(sb.toString());
        message.enableMarkdown(true);
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddProducts(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getCancelCreateProductKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText(
                """
                Меню добавления товара. Чтобы добавить товар, можно отправить ссылку на него.

                В данный момент поддерживается только YandexMarket.
                """
        ); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddProductSuccess(State state, ProductCreationStatus status) {
        InlineKeyboardMarkup kbm = bot.Keyboards.getCancelCreateProductKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        switch (status) {
            case SUCCESS ->
                    message.setText("Продукт успешно добавлен.");
            case UNEXPECTED_MARKET -> message.setText(
                    "Такой магазин пока что не поддерживается.");
            case UNEXPECTED_URL -> message.setText("Данная ссылка ведёт не на страницу товара.");
            case NO_PRODUCT -> message.setText(
                    "Данная страница не содержит товара. Возможно такого товара нет.");
            case FAILED -> message.setText("Произошла ошибка.");
            case FORBIDDEN -> message.setText("Нет прав для добавления в эту группу.");
        }
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageDeleteProducts(State state, List<Product> products) {
        InlineKeyboardMarkup kbm = Keyboards.getDeleteProductsKeyboard(state, products);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Меню удаления товара. Чтобы удалить товар, можно нажать кнопку с его названием. Он сразу удалится.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageDeleteProductSuccess(State state, DeletionStatus status) {
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        switch (status) { // todo: write prod messages
            case SUCCESS -> message.setText("Продукт успешно удалён.");
            case NOT_FOUND -> message.setText("Продукт не удалён (не найдена)");
            case FORBIDDEN -> message.setText("Продукт не удалён (нет доступа)");
        }
        return message;
    }

    static SendMessage getMessageRetrieveProduct(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getRetrieveProductsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Меню информации о товаре.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageResetProducts(State state, List<Product> products) {
        InlineKeyboardMarkup kbm = Keyboards.getResetProductsKeyboard(state, products);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Меню сброса статистики о товаре. После нажатия на кнопку с товаром, вся статистика изменения цены данного товара будет удалена.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageResetProductsSuccess(State state, String productName) {
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText(String.format("Сброшена статистика товара %s.",productName));
        return message;
    }



    static SendMessage getMessageAllProducts(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getAllProductsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Привет. Это меню список всех товаров."); // todo: write prod message
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageAddProductUnexpected(State state) {
        InlineKeyboardMarkup kbm = Keyboards.getAddProductsKeyboard(state);
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Такого сообщения не ожидалось.");
        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getMessageError(State state) {
//        InlineKeyboardMarkup kbm = Keyboards.getMainKeyboard();
        SendMessage message = new SendMessage();
        message.setChatId(state.userTgId);
        message.setText("Произошла ошибка");
//        message.setReplyMarkup(kbm);
        return message;
    }

    static SendMessage getNotificationMessage(long tgId, String productName, double prevPrice, double newPrice) {
        SendMessage message = new SendMessage();
        message.setChatId(tgId);
        message.setText(String.format(
                "Обновлена цена товара.\n\n%s\n\nстарая цена: %.2f руб\n\nновая цена: %.2f руб.", productName, prevPrice, newPrice));
        return message;
    }
}
