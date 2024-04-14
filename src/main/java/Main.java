import org.hibernate.SessionFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        String BOT_TOKEN = System.getenv("BOT_TOKEN");
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new NotificationBot(BOT_TOKEN));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}