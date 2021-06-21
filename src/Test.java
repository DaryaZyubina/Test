import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) throws MessagingException {
        Hashtable<String, String> urls_old = new Hashtable<String, String>();
        Hashtable<String, String> urls_new = new Hashtable<String, String>();

        List url_disappear = new ArrayList();
        List url_add = new ArrayList();
        List url_change = new ArrayList();

        //считаю что первоначально таблицы записаны в файле, 1 строка = 1 сайт, вначале идет url, далее пробел и html
        //заполнение таблиц из двух файлов
        try {
            urls_old = getUrlsFromFile("old.txt");
            urls_new = getUrlsFromFile("new.txt");
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Enumeration enu_new = urls_new.keys();
        Enumeration enu_old = urls_old.keys();
        String url, html;

        while(enu_new.hasMoreElements()) {
            url = (String) enu_new.nextElement();
            html = urls_new.get(url);

            if (urls_old.containsKey(url)){
                if (!html.equals(urls_old.get(url)))
                    url_change.add(url);    //значения не равны, значит они поменялись
            }
            else{
                url_add.add(url); //в старом списке не присутсвует новый юрл -> этот юрл добавился
            }
        }

        while(enu_old.hasMoreElements()){
            url = (String) enu_old.nextElement();

            if (!urls_new.containsKey(url))
                url_disappear.add(url); // в новой таблице нет юрл из старой таблицы -> юрл пропал
        }

        sendEmail(url_disappear, url_add, url_change); //отправка письма

    }

    public static Hashtable<String, String> getUrlsFromFile(String filePath) throws IOException {
        List<String> list;
        Hashtable<String, String> url = new Hashtable<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
        String line;
        String url_line = "";
        String html = "";

        while ((line = bufferedReader.readLine()) != null) {  // считывание файла по 1 строке
            url_line = "";
            html = "";
            list = Arrays.stream(line.split("\\s"))  // производиться разбиение чтобы отделить ключ от содержимого
                    .collect(Collectors.toList());  // собрали в список
            for (int i = 0; i < list.size(); i++) {  // начали перебирать лист
                if (i == 0) {
                    url_line = list.get(i);   // зная, что нулевой элемент это юрл записываем его
                } else {
                    html += list.get(i) + " "; // остальные элементы это код страницы + возвращаем пробелы где были
                }
            }
            url.put(url_line, html); // записываем в таблицу
        }
        return url;
    }

    public static void sendEmail(List url_disappear, List url_add, List url_change) throws MessagingException {
        Properties properties = new Properties();
        //отправка письма расчитана для почт на gmail.com
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");

        String email_from = "alena.fedoseeva@gmail.com";   //введите сюда откуда хотите прислать письмо
        String password = "****";                       // необходимо указать пароль данной почты

        String email_to = "alena.fedoseeva@gmail.com";     //куда необходимо прислать письмо

        Session session= Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email_from, password);
            }
        });

        Message message = createMessage(session, email_from, email_to, url_disappear, url_add, url_change);
        Transport.send(message);
        System.out.println("Email Sent successfully");
    }

    public static Message createMessage(Session session, String email_from, String email_to, List url_disappear, List url_add, List url_change){
        try {
            String dissappear = printUrl(url_disappear);
            String add = printUrl(url_add);
            String change = printUrl(url_change);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email_from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email_to));
            message.setSubject("Мониторинг сайтов");
            message.setText("Здравствуйте, дорогая и.о. секретаря \n " +
                    "За последние сутки во вверенных Вам сайтах произошли следующие изменения:\n" +
                    "\n" +
                    "Исчезли следующие страницы:\n" +
                    dissappear +
                    "\n" +
                    "Появились следующие новые страницы:\n" +
                    add +
                    "\n" +
                    "Изменились следующие страницы:\n" +
                    change +
                    "\n" +
                    "\n" +
                    "С уважением,\n" +
                    "автоматизированная система\n" +
                    "мониторинга.");
            return message;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String printUrl(List list){
        String str;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
                sb.append(list.get(i)).append("; ");

        str = sb.toString();
        return str;
    }
}
