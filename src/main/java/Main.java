import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.util.Objects;
import java.util.Properties;


public class Main {
    static String br = "";
    public static void main(String[] args) {

        Properties properties = new Properties();
        try {
            String patchProperties = new File("data.properties").getAbsolutePath();
            properties.load(new FileReader(patchProperties));
        } catch (IOException e) {
            System.out.println("\"data.properties\" не найден");
            return;
        }
        System.out.println("data.properties Найден");

        String unpackingDirectory = properties.getProperty("unpackingDirectory");
        String clearDirectoryBeforeLoad = properties.getProperty("clearDirectoryBeforeLoad");
        br = properties.getProperty("br"); //<br />  <br>
        System.out.println("unpackingDirectory = " + unpackingDirectory);
        System.out.println("clearDirectoryBeforeLoad = " + clearDirectoryBeforeLoad);

        if (unpackingDirectory.isEmpty() || unpackingDirectory.equals(" ")) {
            System.out.println("неверно указан unpackingDirectory в \"data.properties\"");
            return;
        }
        if (Boolean.parseBoolean(clearDirectoryBeforeLoad)) {
            try {
                delFolder(unpackingDirectory);
            } catch (Exception e) {
                System.out.println("неверно указан unpackingDirectory в \"data.properties\"");
                return;
            }
            System.out.println("директория очищена");
        }
        if (br.isEmpty()){
            System.out.println("Укажите тэг br = <br /> или <br>");
        }


        URL url = getURL();
        if (url == null) {
            System.out.println("URL не найден");
            return;
        }
        System.out.println("URL найден = " + url);

        String[] elementURL = url.getPath().split("/");
        String nameArchive = elementURL[elementURL.length - 1];
        Path allPathArchive = Paths.get(unpackingDirectory + "\\" + nameArchive);
        System.out.println("Имя архива = " + nameArchive);

        System.out.println("Начало скачивания, ожидайте...");
        try {
            downloadFile(url, allPathArchive);
        } catch (IOException e) {
            System.out.println("URL не отвечает");
            return;
        }
        System.out.println("Архив скачан, распаковываем в " + unpackingDirectory);

        unpackZip(allPathArchive.toString(), unpackingDirectory);

        System.out.println("Разархивация прошла успешно");
    }

    public static void delAllFile(String path) throws IOException {
            /*Stream<Path> pathStream = Files.list(path);
            for (Path p : pathStream.toList()) {
                if (Files.isDirectory(p))
                    recursiveDelete(p);
                Files.delete(p);
            }*/
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]); // Сначала удалите файлы в папке
                delFolder(path + "/" + tempList[i]); // Затем удаляем пустую папку
            }
        }
    }

    public static void delFolder(String folderPath) throws IOException {
            delAllFile(folderPath); // Удалить все содержимое
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); // Удалить пустую папку
            Files.createDirectories(Paths.get(folderPath));
    }


        static URL getURL () {
            try {
                Document document = null;
                document = Jsoup.connect("http://btep.org.ru/").get();
                Elements elements = document.select("body > table > tbody > tr:nth-child(1) > td > div:nth-child(4) > div");
                String[] strings = elements.toString().split(br);
                return new URL(strings[7]);
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                System.out.println("Нет подключения к сайту");
                return null;
            }
        }

        public static void downloadFile (URL url, Path path) throws IOException {
            try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                 FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                fileChannel.transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        }

        public static void unpackZip (String zipPath, String beginPatch){
            if (Files.exists(Paths.get(zipPath)))
                ZipUtil.unpack(new File(zipPath), new File(beginPatch));
            else System.out.println("Архив не найден");
        }

    }



