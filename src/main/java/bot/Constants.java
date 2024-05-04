package bot;

public class Constants {

    public static final int PAGE_SIZE = Integer.parseInt(
            System.getenv().getOrDefault("PAGE_SIZE", "5"));
    public static final long ADMIN_ID = Integer.parseInt(System.getenv("ADMIN_ID"));
}
