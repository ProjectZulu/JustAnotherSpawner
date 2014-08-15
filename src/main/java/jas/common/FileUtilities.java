package jas.common;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.google.common.base.Optional;

public class FileUtilities {

    public static final void copy(File source, File destination) {
        if (source.isDirectory()) {
            copyDirectory(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    public static final void copyDirectory(File source, File destination) {
        if (!source.isDirectory()) {
            throw new IllegalArgumentException("Source (" + source.getPath() + ") must be a directory.");
        }

        if (!source.exists()) {
            throw new IllegalArgumentException("Source directory (" + source.getPath() + ") doesn't exist.");
        }

        if (destination.exists()) {
            throw new IllegalArgumentException("Destination (" + destination.getPath() + ") exists.");
        }

        destination.mkdirs();
        File[] files = source.listFiles();

        for (File file : files) {
            copy(file, new File(destination, file.getName()));
        }
    }

    public static final void copyFile(File source, File destination) {
        FileChannel sourceChannel = null;
        FileChannel targetChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            targetChannel = new FileOutputStream(destination).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (sourceChannel != null) {
                    sourceChannel.close();
                }
                if (targetChannel != null) {
                    targetChannel.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static OptionalCloseable<FileWriter> createWriter(File file, boolean createIfAbsent) {
        try {
            if (createIfAbsent && !file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            if (file.exists()) {
                return OptionalCloseable.of(new FileWriter(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return OptionalCloseable.absent();
    }

    public static OptionalCloseable<FileReaderPlus> createReader(File file, boolean createIfAbsent) {
        try {
            if (createIfAbsent && !file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            if (file.exists()) {
                return OptionalCloseable.of(new FileReaderPlus(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return OptionalCloseable.absent();
    }

    public static class OptionalCloseable<T extends Closeable> {
        private Optional<T> object;

        private OptionalCloseable() {
            object = Optional.absent();
        }

        private OptionalCloseable(T value) {
            object = Optional.of(value);
        }

        public boolean isPresent() {
            return object.isPresent();
        }

        public T get() {
            return object.get();
        }

        public void close() {
            if (object.isPresent()) {
                try {
                    object.get().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public static <T extends Closeable> OptionalCloseable<T> absent() {
            return new OptionalCloseable<T>();
        }

        public static <T extends Closeable> OptionalCloseable<T> of(T value) {
            return new OptionalCloseable<T>(value);
        }
    }

	/**
	 * FileWriter that provides access to the underlying File object
	 */
	public static class FileReaderPlus extends FileReader {
		public final File file;

		public FileReaderPlus(File file) throws FileNotFoundException {
			super(file);
			this.file = file;
		}
	}
    
    public static File[] getFileInDirectory(File directory, final String suffix) {
        if (!directory.exists()) {
            return new File[0];
        }
        directory.mkdirs();
        return directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(suffix);
            }
        });
    }
}
