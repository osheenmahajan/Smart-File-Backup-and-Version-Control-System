// Smart File Backup & Version Control System (Console-based in Java)

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class FileVersion {
    String fileName;
    String versionId;
    LocalDateTime timestamp;
    String fileHash;
    String backupPath;

    public FileVersion(String fileName, String versionId, LocalDateTime timestamp, String fileHash, String backupPath) {
        this.fileName = fileName;
        this.versionId = versionId;
        this.timestamp = timestamp;
        this.fileHash = fileHash;
        this.backupPath = backupPath;
    }

    @Override
    public String toString() {
        return versionId + " | " + timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " | Hash: " + fileHash;
    }
}

class FileManager {
    Map<String, Stack<FileVersion>> versionMap = new HashMap<>();
    String backupDir = "backup_storage";

    public FileManager() {
        File dir = new File(backupDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void backupFile(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        String fileName = file.getName();
        String hash = computeHash(file);
        Stack<FileVersion> versions = versionMap.getOrDefault(fileName, new Stack<>());

        if (!versions.isEmpty() && versions.peek().fileHash.equals(hash)) {
            System.out.println("No changes detected. Backup not needed.");
            return;
        }

        String versionId = "v" + (versions.size() + 1);
        LocalDateTime now = LocalDateTime.now();
        String backupFileName = fileName + "_" + versionId;
        Path destPath = Paths.get(backupDir, backupFileName);
        Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

        FileVersion version = new FileVersion(fileName, versionId, now, hash, destPath.toString());
        versions.push(version);
        versionMap.put(fileName, versions);

        System.out.println("Backup successful. Version: " + versionId);
    }

    public void listVersions(String fileName) {
        Stack<FileVersion> versions = versionMap.get(fileName);
        if (versions == null || versions.isEmpty()) {
            System.out.println("No versions found for this file.");
            return;
        }

        for (FileVersion version : versions) {
            System.out.println(version);
        }
    }

    public void restoreVersion(String fileName, String versionId) throws IOException {
        Stack<FileVersion> versions = versionMap.get(fileName);
        if (versions == null || versions.isEmpty()) {
            System.out.println("No versions found for this file.");
            return;
        }

        for (FileVersion version : versions) {
            if (version.versionId.equals(versionId)) {
                Files.copy(Paths.get(version.backupPath), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Restored version " + versionId + " to original file.");
                return;
            }
        }
        System.out.println("Version not found.");
    }
    public void deleteVersion(String fileName, String versionId) throws IOException {
        Stack<FileVersion> versions = versionMap.get(fileName);
        if (versions == null || versions.isEmpty()) {
            System.out.println("No versions found for this file.");
            return;
        }

        Iterator<FileVersion> iterator = versions.iterator();
        while (iterator.hasNext()) {
            FileVersion version = iterator.next();
            if (version.versionId.equals(versionId)) {
                Files.deleteIfExists(Paths.get(version.backupPath));
                iterator.remove();
                System.out.println("Deleted version " + versionId + " of file " + fileName);
                return;
            }
        }
        System.out.println("Version not found.");
    }

    private String computeHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

public class SmartFileBackupSystem {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        FileManager manager = new FileManager();

        while (true) {
            System.out.println("\n==== Smart File Backup & Version Control ====");
            System.out.println("1. Backup a File");
            System.out.println("2. View File Versions");
            System.out.println("3. Restore a Version");
            System.out.println("4. Delete a Version");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter file path to backup: ");
                    String path = sc.nextLine();
                    manager.backupFile(path);
                    break;
                case 2:
                    System.out.print("Enter file name to view versions: ");
                    String name = sc.nextLine();
                    manager.listVersions(name);
                    break;
                case 3:
                    System.out.print("Enter file name: ");
                    String fname = sc.nextLine();
                    System.out.print("Enter version ID to restore: ");
                    String vid = sc.nextLine();
                    manager.restoreVersion(fname, vid);
                    break;
                case 4:
                    System.out.print("Enter file name: ");
                    String dname = sc.nextLine();
                    System.out.print("Enter version ID to delete: ");
                    String dvid = sc.nextLine();
                    manager.deleteVersion(dname, dvid);
                    break;
                case 5:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
