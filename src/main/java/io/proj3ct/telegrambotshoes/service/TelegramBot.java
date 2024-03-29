package io.proj3ct.telegrambotshoes.service;

import io.proj3ct.telegrambotshoes.config.BotConfig;
import io.proj3ct.telegrambotshoes.model.Shoes;
import io.proj3ct.telegrambotshoes.model.User;
import io.proj3ct.telegrambotshoes.repositories.ShoesRepository;
import io.proj3ct.telegrambotshoes.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShoesRepository shoesRepository;
    private final BotConfig botConfig;

    static final String HELP_TEXT = "Вас приветствует лучший продавец кроссовок в России Прокофий Ласкин.\n\n" +
            "Этот бот предназначен для демонстрации ассортимента кроссовок. \n\n" +
            "Функционал бота: \n\n" +
            "Команда /start регистрирует Вас, как пользователя и позволяет пользоваться дальнейшим функционалом ; \n\n" +
            "Команда /assortment предоставит весь ассортимент, который сейчас есть в наличии ; \n\n" +
            "Команда /assortmentbysize предоставит весь ассортимент по требуемому размеру, который сейчас есть в наличии ; \n\n" +
            "Команда /assortmentbyprice предоставит весь ассортимент в пределах указанной цены, который сейчас есть в наличии ; \n\n" +
            "Команда /help Ваш помощник и подробно расскажет о каждой команде ; \n\n" +
            "Команда /mydata позволяет Вам увидеть, какую информацию мы храним о Вас ; \n\n" +
            "Команда /deletedata позволяет Вам удалить информацио о себе из нашей базы данных ; \n\n";

    static final String DELETE_TEXT = "Данные успешно удалены";
    static final String ERROR_TEXT = "Извините, %s, такой команды не существует.";
    static final String NOTPERSON_TEXT = "У нас нет информации о Вас";
    static final String SHOES_BY_PRICE = "Введите максимальную стоимость пары: \n\n" +
            "Формат ввода: \n" +
            "\"Цена%10000\" - если максимальная стоимость 10 тыс. рублей; \n\n" +
            "\"Цена%10500\" - если максимальная стоимость 10,5 тыс. рублей; \n\n" +
            "Запрос вводить без ковычек. Пример: \n" +
            "Цена%13500";

    @Value("${password}")
    private String password;



    @Autowired
    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;

        List<BotCommand> listOfCommands = new ArrayList<>();

        listOfCommands.add(new BotCommand("/start", "Добро пожаловать"));
        listOfCommands.add(new BotCommand("/help", "Получить информацию об использовании"));
        listOfCommands.add(new BotCommand("/assortment", "Показать весь ассортимент"));
        listOfCommands.add(new BotCommand("/assortmentbysize", "По требуемому размеру"));
        listOfCommands.add(new BotCommand("/assortmentbyprice", "По требуемой цене"));
        listOfCommands.add(new BotCommand("/mydata", "Получить информацию о себе"));
        listOfCommands.add(new BotCommand("/deletedata", "Удалить данные о себе"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка листа комманд: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            String[] commandArray = messageText.split("%");

            switch (commandArray[0]) {
                case "/addData":
                    addShoesToBD(chatId, messageText);
                    break;
                case "/start":
                    registerUser(update.getMessage());
                    startCommand(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    registerUser(update.getMessage());
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "/mydata":
                    if (userRepository.findById(chatId).isPresent()) {
                        showData(chatId);
                    } else sendMessage(chatId, NOTPERSON_TEXT);
                    break;
                case "/deletedata":
                    if (userRepository.findById(chatId).isPresent()) {
                        deleteData(chatId);
                        sendMessage(chatId, DELETE_TEXT);
                    } else sendMessage(chatId, NOTPERSON_TEXT);
                    break;
                case "/assortmentbysize":
                    getSizeBoard(chatId, "Выберите нужный размер:");
                    break;
                case "/assortmentbyprice":
                    sendMessage(chatId, SHOES_BY_PRICE);
                    break;
                case "/assortment":
                    showAllAssortment(chatId);
                    break;
                case "Цена":
                    showAllAssortmentByPrice(chatId, Long.parseLong(commandArray[1]));
                    break;
                case "/showAllBD":
                    showAllFromDB(chatId, messageText);
                    break;
                case "/changePrice":
                    changePrice(chatId, messageText);
                    break;
                case "/changeQuantity":
                    changeQuantity(chatId, messageText);
                    break;
                case "/removeFromBD":
                    remove(chatId, messageText);
                    break;
                default:
                    registerUser(update.getMessage());
                    sendMessage(chatId, String.format(ERROR_TEXT,
                            update.getMessage().getChat().getFirstName()));
            }


        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            String[] commandArray = callbackData.split(" ");

            switch (commandArray[0]) {
                case "assortment":
                    showAllAssortment(chatId);
                    break;
                case "assortmentBySize":
                    getSizeBoard(chatId, "Выберите требуемый размер:");
                    break;
                case "assortmentByPrice":
                    sendMessage(chatId, SHOES_BY_PRICE);
                    break;
                case "size":
                    showAllAssortmentBySize(chatId, Double.parseDouble(commandArray[1]));
            }
        }
    }

    //показать данные о пользователе
    private void showData(long chatId) {
        if (userRepository.findById(chatId).isPresent()) {
            User user = findOne(chatId);
            sendMessage(chatId, "Мы храним о Вас следующие данные: \n\n" +
                    "Ваш ChatID: " + user.getChatId() + " ;\n\n" +
                    "Ваше имя: " + user.getFirstName() + " ;\n\n" +
                    "Ваша фамилия: " + user.getLastName() + " ;\n\n" +
                    "Ваше имя пользователя: " + user.getUserName() + " ;\n\n" +
                    "Вы зарегистрировались: " + user.getRegisteredAt() + " ;\n\n" +
                    "Если хотите удалить данные о себе, то воспользуйтесь командой /deletedata");
        }
    }

    //возвращает одного человека
    public User findOne(long chatId) {
        Optional<User> foundPerson = userRepository.findById(chatId); //либо находит, либо нет
        return foundPerson.orElse(null);
    }

    //удалить человека из БД
    private void deleteData(long chatId) {
        userRepository.deleteById(chatId);
    }

    //регистрация человека в БД
    private void registerUser(Message msg) {

        if (userRepository.findById(msg.getChatId()).isEmpty()) {

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Date(System.currentTimeMillis()));

            userRepository.save(user);

            log.info("Сохранен человек: " + user);

        }

    }

    //Команда при нажатии на /start
    private void startCommand(long chatId, String name) {
        String answer = "Привет, " + name + ", добро пожаловать! \n\n" +
                "Для вызова помощи введите /help \n\n" +
                "Для просмотра ассортимента:";


        //добавим запись о том, что ответили пользователю
        log.info("Ответили пользователю " + name);

        sendMenuMessage(chatId, answer);
    }


    //Кнопки после старт
    private void sendMenuMessage(long chatId, String textToSend) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        //клавиатура для ответов
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var assortimentButton = new InlineKeyboardButton();
        assortimentButton.setText("Весь ассортимент");
        assortimentButton.setCallbackData("assortment х");

        var assortmentBySizeButton = new InlineKeyboardButton();
        assortmentBySizeButton.setText("Найти по размеру");
        assortmentBySizeButton.setCallbackData("assortmentBySize х");

        var assortmentByPriceButton = new InlineKeyboardButton();
        assortmentByPriceButton.setText("Найти по цене");
        assortmentByPriceButton.setCallbackData("assortmentByPrice х");

        rowInLine.add(assortimentButton);
        rowInLine.add(assortmentBySizeButton);
        rowInLine.add(assortmentByPriceButton);

        rowsInLine.add(rowInLine);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Случилась ошибка: " + e.getMessage());
        }

    }

    //Отправка сообщения
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Случилась ошибка: " + e.getMessage());
        }
    }

    //кнопки с размерами
    private void getSizeBoard(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine3 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine4 = new ArrayList<>();

        var size5_5 = new InlineKeyboardButton();
        size5_5.setText("5.5 UK");
        size5_5.setCallbackData("size 5.5");

        var size6 = new InlineKeyboardButton();
        size6.setText("6 UK");
        size6.setCallbackData("size 6.0");

        var size6_5 = new InlineKeyboardButton();
        size6_5.setText("6.5 UK");
        size6_5.setCallbackData("size 6.5");

        var size7 = new InlineKeyboardButton();
        size7.setText("7 UK");
        size7.setCallbackData("size 7.0");

        Collections.addAll(rowInLine1, size5_5, size6, size6_5, size7);
        rowsInLine.add(rowInLine1);

        var size7_5 = new InlineKeyboardButton();
        size7_5.setText("7.5 UK");
        size7_5.setCallbackData("size 7.5");

        var size8 = new InlineKeyboardButton();
        size8.setText("8 UK");
        size8.setCallbackData("size 8");

        var size8_5 = new InlineKeyboardButton();
        size8_5.setText("8.5 UK");
        size8_5.setCallbackData("size 8.5");

        var size9 = new InlineKeyboardButton();
        size9.setText("9 UK");
        size9.setCallbackData("size 9.0");

        Collections.addAll(rowInLine2, size7_5, size8, size8_5, size9);
        rowsInLine.add(rowInLine2);

        var size9_5 = new InlineKeyboardButton();
        size9_5.setText("9.5 UK");
        size9_5.setCallbackData("size 9.5");

        var size10 = new InlineKeyboardButton();
        size10.setText("10 UK");
        size10.setCallbackData("size 10.0");

        var size10_5 = new InlineKeyboardButton();
        size10_5.setText("10.5 UK");
        size10_5.setCallbackData("size 10.5");

        var size11 = new InlineKeyboardButton();
        size11.setText("11 UK");
        size11.setCallbackData("size 11.0");

        Collections.addAll(rowInLine3, size9_5, size10, size10_5, size11);
        rowsInLine.add(rowInLine3);

        var size11_5 = new InlineKeyboardButton();
        size11_5.setText("11.5 UK");
        size11_5.setCallbackData("size 11.5");

        var size12 = new InlineKeyboardButton();
        size12.setText("12 UK");
        size12.setCallbackData("size 12.0");

        var size12_5 = new InlineKeyboardButton();
        size12_5.setText("12.5 UK");
        size12_5.setCallbackData("size 12.5");

        var size13 = new InlineKeyboardButton();
        size13.setText("13 UK");
        size13.setCallbackData("size 13.0");

        Collections.addAll(rowInLine4, size11_5, size12, size12_5, size13);
        rowsInLine.add(rowInLine4);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Случилась ошибка: " + e.getMessage());
        }
    }

    //получить фотку кроссовок
    private void getPhotoShoes(long chatId, String photoPath, String caption) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(chatId));
        photo.setPhoto(new InputFile(photoPath));
        photo.setCaption(caption);

        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Случилась ошибка: " + e.getMessage());
        }
    }


    public void shoesList(List<Shoes> shoesList, Long chatId){
        for(Shoes shoes : shoesList) {
            String message = shoes.getName() + "\n" +
                    "Размер: " + shoes.getSize() + "\n" +
                    "Цена: " + shoes.getPrice();
            getPhotoShoes(chatId, shoes.getReference(), message);

        }
    }
    public void showAllAssortment(Long chatId){
        List<Shoes> shoesList = shoesRepository.findAll();
        shoesList(shoesList, chatId);
    }

    public void showAllAssortmentBySize(Long chatId, double size){
        List<Shoes> shoesList = shoesRepository.findAllBySizeAndQuantityAfter(size, 0);
        if (!shoesList.isEmpty())
        shoesList(shoesList, chatId);
        else sendMessage(chatId,"В наличии нет модели размера " + size + " UK");
    }

    public void showAllAssortmentByPrice(Long chatId, long price){
        List<Shoes> shoesList = shoesRepository.findAllByPriceIsBeforeAndQuantityAfter(price, 0);
        List<Shoes> shoesList2 = shoesRepository.findAllByPriceIsAndQuantityAfter(price, 0);
        shoesList.addAll(shoesList2);

        if (!shoesList.isEmpty())
            shoesList(shoesList, chatId);
        else sendMessage(chatId,"В наличии нет модели дешевле " + price);
    }

    /*
    Базы данных
    */

    //добавить картинку
    public void addShoesToBD(Long chatId, String messageText) {
        Shoes shoes = new Shoes();

        String[] str = messageText.split("%");

        if (str[1].equals(this.password)) {

            shoes.setName(str[2]);

            shoes.setSize(Double.parseDouble(str[3]));

            shoes.setPrice(Long.parseLong(str[4]));

            shoes.setQuantity(Integer.parseInt(str[5]));

            shoes.setReference(str[6]);

            shoesRepository.save(shoes);

            sendMessage(chatId, "Insertion completed");
        } else sendMessage(chatId, "You don't have permission");
    }

    //Показать все, что есть в бд
    public void showAllFromDB(Long chatId, String messageText){

        String[] str = messageText.split("%");

        if (str[1].equals(this.password)) {
            List<Shoes> allShoes = shoesRepository.findAll();
            for (Shoes shoes : allShoes) {
                sendMessage(chatId,
                        "id: " + shoes.getShoesId() + " | " +
                                shoes.getName() + "\n" +
                                "Размер: " + shoes.getSize() + " | " +
                                "Цена: " + shoes.getPrice() + " | " +
                                "Кол-во: " + shoes.getQuantity() + "\n\n"
                );
            }
        } else sendMessage(chatId, "You don't have permission");
    }

    //Вернуть одну пару кроссовок
    public Shoes findOne(Long shoesId) {
        Optional<Shoes> foundShoes = shoesRepository.findById(shoesId);
        return foundShoes.orElse(null);
    }

    //Изменить цену
    public void changePrice(Long chatId,  String messageText){

        String[] str = messageText.split("%");

        if (str[1].equals(this.password)) {

            Long shoesId = Long.parseLong(str[2]);
            Long price = Long.parseLong(str[3]);

            Shoes shoes = findOne(shoesId);
            if (shoes != null) {
                shoes.setPrice(price);
                shoesRepository.save(shoes);
                sendMessage(chatId, "Price is changed");
            } else sendMessage(chatId, "This element is not in the database");
        } else sendMessage(chatId, "You don't have permission");
    }

    //Изменить кол-во
    public void changeQuantity(Long chatId,  String messageText){

        String[] str = messageText.split("%");

        if (str[1].equals(this.password)) {

            Long shoesId = Long.parseLong(str[2]);
            int quantity = Integer.parseInt(str[3]);

            Shoes shoes = findOne(shoesId);
            if (shoes != null) {
                shoes.setQuantity(quantity);
                shoesRepository.save(shoes);
                sendMessage(chatId, "Quantity is changed");
            } else sendMessage(chatId, "This element is not in the database");
        } else sendMessage(chatId, "You don't have permission");
    }

    //Удалить из БД
    public void remove(Long chatId, String messageText){
        String[] str = messageText.split("%");
        if (str[1].equals(this.password)) {
            Long shoesId = Long.parseLong(str[2]);
            shoesRepository.deleteById(shoesId);
            sendMessage(chatId, "Shoes removed");
        } else sendMessage(chatId, "You don't have permission");
    }
}
