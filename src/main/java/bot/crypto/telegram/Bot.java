package bot.crypto.telegram;

import bot.crypto.services.CreateMessageService;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

/**
 * @author Taras Hryniuk, created on  21.10.2020
 * email : hryniuk.t@gmail.com
 */
public class Bot extends TelegramLongPollingBot {

    private static final Logger LOGGER = Logger.getLogger(Bot.class);

    final int RECONNECT_PAUSE = 100;

    @Setter
    @Getter
    String userName;
    @Setter
    @Getter
    String token;

    public Bot(String userName, String token) {
        this.userName = userName;
        this.token = token;
    }

    @Override
    public void onUpdateReceived(Update update) {

        Long chatId = update.getMessage().getChatId();
        String inputText = update.getMessage().getText().toUpperCase();

        StringBuilder sb = new StringBuilder();
        sb.append("Receive new Update. updateID: ").append(update.getUpdateId()).append(" ,userName: ")
                .append(update.getMessage().getFrom().getUserName()).append(" ,userId: ").append(update.getMessage().getFrom().getId())
                .append(" ,location: ").append(update.getMessage().getLocation() + ", coin: " + inputText);
        LOGGER.info(sb.toString());

        CreateMessageService createMessageService = new CreateMessageService();

        if (inputText.contains("/CRYPTO"))
            inputText = inputText.split("\\s+")[1];

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(createMessageService.createMessage(inputText, update.getMessage().getFrom().getUserName()
                , update.getMessage().getFrom().getId(), update.getUpdateId()));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error(e);
        }
    }

    @Override
    public String getBotUsername() {
        return userName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public void botConnect() {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(this);
            LOGGER.info("TelegramAPI started. Look for messages");
        } catch (TelegramApiRequestException e) {
            LOGGER.error("Cant Connect. Pause " + RECONNECT_PAUSE / 1000 + "sec and try again. Error: " + e.getMessage());
            try {
                Thread.sleep(RECONNECT_PAUSE);
            } catch (InterruptedException e1) {
                LOGGER.error(e1);
                return;
            }
            botConnect();
        }
    }
}
