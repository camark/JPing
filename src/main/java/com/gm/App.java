package com.gm;

import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 *
 */
public class App 
{
    public static boolean ping(String ipAddress) throws Exception {
        int  timeOut =  3000 ;  //超时应该在3钞以上
        boolean status = InetAddress.getByName(ipAddress).isReachable(timeOut);     // 当返回值是true时，说明host是可用的，false则不可。
        return status;
    }

    public static void ping02(String ipAddress) throws Exception {
        String line = null;
        try {
            Process pro = Runtime.getRuntime().exec("ping " + ipAddress);
            BufferedReader buf = new BufferedReader(new InputStreamReader(
                    pro.getInputStream()));
            while ((line = buf.readLine()) != null)
                System.out.println(line);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static boolean ping(String ipAddress, int pingTimes, int timeOut) {
        BufferedReader in = null;
        Runtime r = Runtime.getRuntime();  // 将要执行的ping命令,此命令是windows格式的命令
        String pingCommand = "ping " + ipAddress + " -n " + pingTimes    + " -w " + timeOut;
        try {   // 执行命令并获取输出
            System.out.println(pingCommand);
            Process p = r.exec(pingCommand);
            if (p == null) {
                return false;
            }
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));   // 逐行检查输出,计算类似出现=23ms TTL=62字样的次数
            int connectedCount = 0;
            String line = null;
            while ((line = in.readLine()) != null) {
                connectedCount += getCheckResult(line);
            }   // 如果出现类似=23ms TTL=62这样的字样,出现的次数=测试次数则返回真
            return connectedCount == pingTimes;
        } catch (Exception ex) {
            ex.printStackTrace();   // 出现异常则返回假
            return false;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //若line含有=18ms TTL=16字样,说明已经ping通,返回1,否則返回0.
    private static int getCheckResult(String line) {  // System.out.println("控制台输出的结果为:"+line);
        Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)",    Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            return 1;
        }
        return 0;
    }


    private final Logger logger=Logger.getLogger(this.getClass().getName());
    private static Options options = new Options();
    public static void main(String[] args) throws Exception {

        options.addOption("l","level",true,"选择" +
                "网段（1-5）");

        CommandLineParser commandLineParser=new DefaultParser();
        CommandLine cmd=commandLineParser.parse(options,args);

        int level=3;
        if(cmd.hasOption("l")){
            String inputValue=cmd.getOptionValue("l");
            //System.out.println(inputValue);
            level=Integer.parseInt(inputValue);
            //System.out.println(level);

            if(level<1 || level>5){
                System.out.println("level must between 1 and 5");
                //logger.info("paramer error!")
                return;
            }
        }
        final Set<String> ip_addresses=new HashSet<>();

        ExecutorService threadPool = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch=new CountDownLatch(253);
        String prev_IP="10.10."+level+".";
        for(int i=2;i<255;i++){
            final String dest_ip=prev_IP+i;

            /*Future<Boolean> ping_addrs=threadPool.submit(new Callable<Boolean>()
            {
                @Override
                public Boolean call() throws Exception {
                    return ping(dest_ip);
                }
            });

            if(ping_addrs.get()){
                ip_addresses.add(dest_ip);
                System.out.println(dest_ip);
            }*/

            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    boolean bExist=false;
                    try {
                        bExist=ping(dest_ip);
                    }catch (Exception exp) {
                        //System.out.println("error:"+exp.getMessage());
                    }
                    countDownLatch.countDown();

                    if(bExist){
                        System.out.println(dest_ip);
                    }
                }
            });
        }
    }
}
