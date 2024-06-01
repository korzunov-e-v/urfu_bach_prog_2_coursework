import bot.NotificationBot;
import database.HibernateUtil;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import scheduler.Scheduler;

public class Main {

    public static void main(String[] args) {
        String BOT_TOKEN = System.getenv("BOT_TOKEN");
        HibernateUtil.getSessionFactory();

        Scheduler.main();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new NotificationBot(BOT_TOKEN));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}