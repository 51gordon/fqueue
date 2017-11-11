package com.github.cgdon.fqueue.demo;

import java.util.UUID;

import com.github.cgdon.fqueue.FQueue;

/**
 *
 * @author HeDYn <a href='mailto:hedyn@foxmail.com'>hedyn</a>
 */
public class FQueueDemo {

    private static boolean writeEnd = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        FQueue queue = new FQueue("fqdb", 4 * 1024 * 1024);
        int len = 100;
        uuidArr = new String[len];
        Thread writer = new Thread(new WriteRunnable(queue, len, 0));
        Thread reader = new Thread(new ReadRunnable(queue));
        writer.start();
        try {
            Thread.sleep(200);
        } catch (Exception ex) {
        }
        reader.start();
    }

    private static String[] uuidArr;

    private static class WriteRunnable implements Runnable {

        private FQueue queue;
        private int size;
        private long sleep;

        public WriteRunnable(FQueue queue, int size, long sleep) {
            this.queue = queue;
            this.size = size;
            this.sleep = sleep;
        }

        @Override
        public void run() {
            int counter = 0;
            long now = System.currentTimeMillis();
            for (int i = 0; i < size; i++) {
                String uuid = i + ": " + UUID.randomUUID().toString();
                uuidArr[i] = uuid;
                //System.out.println("W: " + uuid);
                boolean b = queue.offer(uuid.getBytes());
                if (b) {
                    counter++;
                }
                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (Exception ex) {
                    }
                }
            }
            System.out.println("W 耗时：" + (System.currentTimeMillis() - now) + ", " + counter);
            writeEnd = true;
        }
    }

    private static class ReadRunnable implements Runnable {

        private FQueue queue;

        public ReadRunnable(FQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            int size = 0;
            int size2 = 0;
            long now = System.currentTimeMillis();
            while (true) {
                byte[] bytes = queue.peek();
                if (bytes != null) {
                    String uuid = new String(bytes);
                    if (!uuid.equals(uuidArr[size])) {
                        System.out.println("error: " + size + ", " + uuid);
                    }
                    size++;

                    bytes = queue.poll();
                    String uuid2 = new String(bytes);
                    if (!uuid.equals(uuid2)) {
                        System.out.println("error: " + size2 + ", " + uuid + " != " + uuid2);
                    }
                    size2++;
                } else if (writeEnd) {
                    break;
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                    }
                }
            }
            System.out.println("R 耗时：" + (System.currentTimeMillis() - now) + ", " + size + ", " + size2);
            try {
                queue.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
