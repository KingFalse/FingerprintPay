package com.cnwy.wechatbot;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;

import com.alibaba.fastjson2.JSON;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WechatHook implements IXposedHookLoadPackage {

    // 微信数据库包名称
    static final String WECHAT_DATABASE_PACKAGE_NAME = "com.tencent.wcdb.database.SQLiteDatabase";
    // 微信主进程名
    static final String WECHAT_PROCESS_NAME = "com.tencent.mm";
    // hook函数
    static final String METHOD_NAME = "insertWithOnConflict";
    static final String METHOD_NAME_INSERT = "insert";
    // 关键key
    static final String CRUX_KEY = "content";
    static final String CRUX_KEY_MESSAGE = "message";

    static final String TAG = "XposedWeixin--->";

    static final String SHI_CI = "https://v2.jinrishici.com/one.json?client=browser-sdk/1.2";

    static volatile int c = 0;


//    private static void weChatLogOpen(final ClassLoader classLoader) {
//        //isLogcatOpen
//        XposedHelpers.findAndHookMethod("com.tencent.mm.xlog.app.XLogSetup", classLoader, "keep_setupXLog", Boolean.class, String.class, String.class, Integer.class, Boolean.class, Boolean.class, String.class, new XC_MethodHook() {
//
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                param.args[5] = true;
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                param.args[5] = true;
//                super.afterHookedMethod(param);
//                Log.i(TAG, "keep_setupXLog参数isLogcatOpen: " + param.args[5]);
//            }
//        });
//    }

    // 获取目标应用 VersionName
    private static String getPackageVersion(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> parserCls = XposedHelpers.findClass("android.content.pm.PackageParser", lpparam.classLoader);
            Object parser = parserCls.newInstance();
            File apkPath = new File(lpparam.appInfo.sourceDir);
            Object pkg = XposedHelpers.callMethod(parser, "parsePackage", apkPath, 0);
            String versionName = (String) XposedHelpers.getObjectField(pkg, "mVersionName");
            int versionCode = XposedHelpers.getIntField(pkg, "mVersionCode");
            return String.format("%s (%d)", versionName, versionCode);
        } catch (Throwable e) {
            return "(unknown)";
        }
    }

    public static boolean containsAnyKeyword(String input, ArrayList<String> keywords) {
        for (String keyword : keywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * hook 微信数据库
     *
     * @param loadPackageParam
     */
    public static void hookDB(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!loadPackageParam.processName.equals(WECHAT_PROCESS_NAME)) {
            return;
        }
        Log.d(TAG, "进入微信进程：" + loadPackageParam.processName);
        Log.d(TAG, "进入微信进程：版本：" + getPackageVersion(loadPackageParam));


//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.wallet_core.ui.WalletPwdConfirmUI", loadPackageParam.classLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                XposedBridge.log("进入支付密码确认界面=================================");
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//            }
//        });

//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.wallet_core.ui.WalletPwdConfirmUI$b", loadPackageParam.classLoader, "onClick", android.view.View.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                XposedBridge.log("点击支付密码确认界面=================================");
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//            }
//        });

//        XposedHelpers.findAndHookMethod("com.tencent.kinda.framework.widget.base.KindaPwdInputViewImpl", loadPackageParam.classLoader, "createView", android.content.Context.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                XposedBridge.log("开始创建密码输入窗口=================================");
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//            }
//        });
//
//        XposedHelpers.findAndHookMethod("com.tencent.kinda.framework.widget.base.KindaPwdInputViewImpl$1", loadPackageParam.classLoader, "onInputValidChange", boolean.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                XposedBridge.log("密码输入事件=================================");
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//            }
//        });


//        XposedHelpers.findAndHookMethod("com.tencent.mm.wallet_core.ui.formview.EditHintPasswdView", loadPackageParam.classLoader, "getMd5Value", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                XposedBridge.log("开始获取支付密码=================================");
//                XposedBridge.log("开始获取支付密码=================================");
//                XposedBridge.log("开始获取支付密码=================================");
//                XposedBridge.log("开始获取支付密码=================================");
//                XposedBridge.log("开始获取支付密码=================================");
//                // 方法五:
//                // Thread类的getAllStackTraces（）方法获取虚拟机中所有线程的StackTraceElement对象，可以查看堆栈
//                for (Map.Entry<Thread, StackTraceElement[]> stackTrace:Thread.getAllStackTraces().entrySet())
//                {
//                    Thread thread = (Thread) stackTrace.getKey();
//                    StackTraceElement[] stack = (StackTraceElement[]) stackTrace.getValue();
//
//                    // 进行过滤
//                    if (thread.equals(Thread.currentThread())) {
//                        continue;
//                    }
//
//                    Log.i("[Dump Stack]","**********Thread name：" + thread.getName()+"**********");
//                    int index = 0;
//                    for (StackTraceElement stackTraceElement : stack) {
//
//                        Log.i("[Dump Stack]"+index+": ", stackTraceElement.getClassName()
//                                +"----"+stackTraceElement.getFileName()
//                                +"----" + stackTraceElement.getLineNumber()
//                                +"----" +stackTraceElement.getMethodName());
//                    }
//                    // 增加序列号
//                    index++;
//                }
//                Log.i("[Dump Stack]","********************* over **********************");
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//            }
//        });


        Class<?> classDb = XposedHelpers.findClassIfExists(WECHAT_DATABASE_PACKAGE_NAME, loadPackageParam.classLoader);
        if (classDb == null) {
            Log.d(TAG, "hook数据库insert操作：未找到类" + WECHAT_DATABASE_PACKAGE_NAME);
            return;
        }

        try {
            //自动点击”关注公众号“按钮
            Class y1 = loadPackageParam.classLoader.loadClass("com.tencent.mm.storage.y1");
            Class cVar = loadPackageParam.classLoader.loadClass("kh.c");
            Class hhVar = loadPackageParam.classLoader.loadClass("dm3.hh");
            XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.profile.ui.tab.ContactWidgetTabBizHeaderController", loadPackageParam.classLoader, "h", y1, String.class, cVar, hhVar, int.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
                    //调用函数
                    //自动点击”关注公众号“按钮
//                    XposedHelpers.callMethod(param.thisObject, "e");
                    XposedHelpers.callMethod(param.thisObject, "d");

                }
            });
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.report.service.o", loadPackageParam.classLoader, "h", int.class, java.lang.Object[].class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
////                super.beforeHookedMethod(param);
//                return;
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//            }
//        });


        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.chatting.ChattingUI", loadPackageParam.classLoader, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                //自动跳转到公众号列表页面
//                Intent intent = new Intent("com.tencent.mm", Uri.parse("com.tencent.mm.ui.contact.ChatroomContactUI"));
//                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("content://com.tencent.mm.ui.contact.ChatroomContactUI"));
                Intent intent = new Intent();
//                intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.contact.ChatroomContactUI");
//                intent.setClassName("com.tencent.mm", "com.tencent.mm.com.tencent.mm.ui.LauncherUI");
                intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                XposedHelpers.callMethod(param.thisObject, "startActivityForResult", intent, -1, null);


//                Intent intent = new Intent();
//                intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.contact.ChatroomContactUI");
//                intent.setClassName("com.tencent.mm", "com.tencent.mm.com.tencent.mm.ui.LauncherUI");
//                intent.setClassName("com.tencent.mm", "com.tencent.mm.plugin.profile.ui.ContactInfoUI");
//                intent.putExtra("Contact_User",username);
//            .currentApplication().getApplicationContext();
                Class<?> aClass = XposedHelpers.findClass("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader);
                Object currentApplication = XposedHelpers.callStaticMethod(aClass, "getInstance");
//                XposedHelpers.callMethod(currentApplication, "closeChatting", false);
                XposedHelpers.callMethod(currentApplication, "startActivityForResult", intent, -1, null);


            }
        });

//
//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.profile.ui.ContactInfoUI", loadPackageParam.classLoader, "getResourceId", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                Log.d(TAG, "参数添加完毕：" + JSON.toJSONString(param.getResult()));
//            }
//        });


//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.profile.ui.ContactInfoUI", loadPackageParam.classLoader, "superImportUIComponents", java.util.HashSet.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                ((HashSet) param.args[0]).add(XposedHelpers.findClass("com.tencent.mm.feature.revoke.RevokeChattingLandingPageUIC",loadPackageParam.classLoader));
//                Log.d(TAG, "参数添加完毕：" + JSON.toJSONString(param.args[0]));
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//            }
//        });


//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.profile.ui.ContactInfoUI", loadPackageParam.classLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//                XposedHelpers.callMethod(param.thisObject,"setRequestedOrientation",1);
//            }
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                XposedHelpers.callMethod(param.thisObject,"setRequestedOrientation",1);
//            }
//        });


//
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.profile.ui.ContactInfoUI", loadPackageParam.classLoader, "onStart", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        if (!isFromAddFriend) {
//                            return;
//                        }

                        try {
                            Object oHt = XposedHelpers.findField(XposedHelpers.findClass("com.tencent.mm.plugin.profile.ui.ContactInfoUI", loadPackageParam.classLoader), "e").get(param.thisObject);
                            XposedHelpers.callMethod(oHt, "a", "contact_profile_add_contact");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1000);


//                new Thread(() -> {
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                    Intent intent = new Intent();
//                    intent.setClassName("com.tencent.mm", "com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI");
//                    intent.putExtra("definitelyEmpty", false);
//                    intent.putExtra("empty", false);
//                    intent.putExtra("emptyParcel", false);
//                    intent.putExtra("parcelled", false);
//                    intent.putExtra("mFlags", 1537);
//                    intent.putExtra("size", 0);
//                    intent.putExtra("stability", 0);
//
//                    Class<?> aClass = XposedHelpers.findClass("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader);
//                    Object currentApplication = XposedHelpers.callStaticMethod(aClass, "getInstance");
//                    XposedHelpers.callMethod(currentApplication, "startActivity", intent);
//                }).start();
            }
        });


//        weChatLogOpen(loadPackageParam.classLoader);

        try {
            XposedHelpers.findAndHookMethod(classDb, METHOD_NAME_INSERT, String.class, String.class, ContentValues.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    pushData(loadPackageParam, param);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, String.format("hookDB err : %s", e));
        }
//        try {
//            XposedHelpers.findAndHookMethod("ne0.d0$f", loadPackageParam.classLoader, "a", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    Log.d(TAG,"========================================"+JSON.toJSONString(param.thisObject));
//                    super.beforeHookedMethod(param);
//                }
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//                }
//            });
//        } catch (Exception e) {
//            Log.d(TAG, String.format("hookDB err : %s", e));
//        }
        Log.d(TAG, "---------------------------------------------------");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(5000);
//                        Log.d(TAG, String.format("开始请求接口 alert get"));
//                        String body = Jsoup.connect("http://11.11.11.147:8080/api/alert/get").ignoreContentType(true).execute().body();
//                        Log.d(TAG, String.format("开始请求接口 alert get" + body));
//
//                        List<Map> maps = JSONArray.parseArray(body, Map.class);
////                    for (Map map : maps) {
////                        String wxid = map.get("wxid").toString();
////                        String msg = map.get("msg").toString();
////                        pushMsg(loadPackageParam, wxid, msg);
////                    }
//                        Map map1 = maps.get(0);
//                        String wxid = map1.get("wxid").toString();
//                        String msg = map1.get("msg").toString();
//                        pushMsg(loadPackageParam, wxid, msg);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Log.d(TAG, String.format("Exception====================================== err : %s", e));
//                    }
//
//                }
//            }
//        }).start();
    }

    public static String convertToXml(Object object) throws Exception {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", true);
        serializeObject(serializer, object);
        serializer.endDocument();
        return writer.toString();
    }

    private static void serializeObject(XmlSerializer serializer, Object object) throws Exception {
        Class<?> clazz = object.getClass();
        serializer.startTag(null, clazz.getSimpleName());

        // 获取对象的属性并将其转换为XML元素
        // 这里需要根据你的具体对象结构进行实现
        // 例如，可以使用反射来获取对象的字段和方法，并将其转换为XML元素

        serializer.endTag(null, clazz.getSimpleName());
    }

    /**
     * 推送数据上报
     *
     * @param param
     */
    private static void pushData(XC_LoadPackage.LoadPackageParam loadPackageParam, XC_MethodHook.MethodHookParam param) {
        String tableName = (String) param.args[0];
        String tableName2 = (String) param.args[1];


        Log.d(TAG, "入库参数1：" + tableName);
        Log.d(TAG, "入库参数2：" + tableName2);
        Log.d(TAG, "入库参数3：" + JSON.toJSONString(param.args[2]));


        ContentValues contentValues = (ContentValues) param.args[2];

        if (!contentValues.containsKey("type")) return;

        Integer type = contentValues.getAsInteger("type");

        if (type == 42) {
            Log.d(TAG, "接收到朋友发的公众号名片消息：" + JSON.toJSONString(param.args[2]));
            //{"empty":false,"stability":0,"values":{"msgSvrId":7958457805757215000,"isSend":0,"msgSeq":779631761,"status":3,"talker":"wxid_rsit34ojcum522","talkerId":3546,"lvbuffer":"ewAAAAAAAAB2PG1zZ3NvdXJjZT4KCTxzaWduYXR1cmU+djFfajFKeWE1WXg8L3NpZ25hdHVyZT4KCTx0bXBfbm9kZT4KCQk8cHVibGlzaGVyLWlkPjwvcHVibGlzaGVyLWlkPgoJPC90bXBfbm9kZT4KPC9tc2dzb3VyY2U+CgAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB9","flag":0,"type":42,"msgId":67508,"bizChatId":-1,"content":"&lt;?xml version=\"1.0\"?&gt;\n&lt;msg bigheadimgurl=\"http://wx.qlogo.cn/mmhead/Q3auHgzwzM4YicI5sXcrg0l7lp3COj9EEp71Z1Mk3YBo4X45BR0FVxg/0\" smallheadimgurl=\"http://wx.qlogo.cn/mmhead/Q3auHgzwzM4YicI5sXcrg0l7lp3COj9EEp71Z1Mk3YBo4X45BR0FVxg/96\" username=\"gh_5943a2470bc4\" nickname=\"北航就业\" fullpy=\"北航就业\" shortpy=\"BHJY\" alias=\"\" imagestatus=\"3\" scene=\"17\" province=\"北京\" city=\"海淀\" sign=\"\" sex=\"0\" certflag=\"24\" certinfo=\"北京航空航天大学\" brandIconUrl=\"http://mmbiz.qpic.cn/mmbiz_png/wLeF4yH3xLArgOMXep0CBKTO7zdI1vkkPiaWdqqiaYHEggYZsxJtlnEh2OUDqERLKgd4nLiapTZojDRZtW1pAA8Hw/0?wx_fmt=png\" brandHomeUrl=\"\" brandSubscriptConfigUrl=\"\" brandFlags=\"0\" regionCode=\"CN_Beijing_Haidian\" biznamecardinfo=\"ClBDZ3psakpmb2lLcmxzTEhrdUpvUUdCb0FJZ0V4S01tYXU2Z0dNZzluYUY4MU9UUXpZVEkwTnpCaVl6UTZERmxpTW5SSllXVXphRzl1VkE9PRJ4QUFmbVBjd0VBQUFCQUFBQUFBQ01xTFByUjBVZitVV2lTYzBPWlNBQUFBQnRFTnNOT1ErQzI0NWQycHFLTlBCUWZRalV3T2R0TlJCaFhzTTl4T3RhMXAvcHQwUEVyVDArL0lyYS9Oalcwdy9NcXczSkQ4VmowRDhP\" /&gt;\n","bizClientMsgId":"","createTime":1695468878000}}
            String content = contentValues.getAsString("content");
            String talker = contentValues.getAsString("talker");
            System.err.println(content);
            Element msg = Jsoup.parse(content).selectFirst("msg");
            String username = msg.attr("username");
            String nickname = msg.attr("nickname");
            replyTextMessage(loadPackageParam, username, talker);

            //自动跳转到公众号列表页面
//                Intent intent = new Intent("com.tencent.mm", Uri.parse("com.tencent.mm.ui.contact.ChatroomContactUI"));
//                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("content://com.tencent.mm.ui.contact.ChatroomContactUI"));
            Intent intent = new Intent();
//                intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.contact.ChatroomContactUI");
//                intent.setClassName("com.tencent.mm", "com.tencent.mm.com.tencent.mm.ui.LauncherUI");
            intent.setClassName("com.tencent.mm", "com.tencent.mm.plugin.profile.ui.ContactInfoUI");
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Contact_User", username);
//            intent.putExtra("definitelyEmpty", false);
//            intent.putExtra("empty", false);
//            intent.putExtra("emptyParcel", false);
//            intent.putExtra("parcelled", false);
//            intent.putExtra("mFlags", 1537);
//            intent.putExtra("size", 0);
//            intent.putExtra("stability", 0);

//            .currentApplication().getApplicationContext();
            Class<?> aClass = XposedHelpers.findClass("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader);
            Object currentApplication = XposedHelpers.callStaticMethod(aClass, "getInstance");
            XposedHelpers.callMethod(currentApplication, "startActivity", intent);


        }


//        if (tableName.equals("message") && type != null && type == 1) {
//            String talker = contentValues.getAsString("talker");
//            String content = contentValues.getAsString("content");
//            Log.d(TAG, String.format("收到纯文本消息 : 来自:%s , %s", talker, content));
//            if (content.trim().startsWith("测试")) {
//                pushMsg(loadPackageParam, talker, "绑定成功啦！" + content);
//            }
////            if (content.trim().contains("@鹞之神乐")&&talker.contains("19381458773@chatroom")) {
////                Log.d(TAG, String.format("收到纯文本消息------------------------ : 来自:%s , %s", talker, content));
////                new Thread(new Runnable() {
////                    @Override
////                    public void run() {
////                        try {
////                            HashMap<String, Object> map = new HashMap<>();
////                            map.put("model","text-davinci-003");
////                            map.put("prompt",content.split("@鹞之神乐")[1]);
////                            map.put("temperature",0);
////                            map.put("max_tokens",3000);
////                            map.put("top_p",1);
////                            map.put("frequency_penalty",0.0);
////                            map.put("presence_penalty",0.0);
////                            map.put("stream",false);
////                            String body = Jsoup.connect("https://api.openai.com/v1/completions")
////                                    .header("Content-Type", "application/json")
////                                    .header("Authorization", "Bearer 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")
////                                    .requestBody(JSON.toJSONString(map))
////                                    .ignoreContentType(true).method(Connection.Method.POST).execute().body();
////                            Integer count = (Integer) JSONPath.read(body, "$.choices.size()");
////                            System.err.println(count);
////                            System.err.println(body);
////                            for (int i = 0; i < count; i++) {
////                                String text = (String) JSONPath.read(body, "$.choices["+i+"].text");
////                                System.err.println(text);
////                                pushMsg(loadPackageParam, "19381458773@chatroom", text);
////                            }
////                        } catch (Exception e) {
////                            Log.d(TAG, "hookDB get data err",e);
////                        }
////
////                    }
////                }).start();
////            }
//        }


        String accountName = "";
        String info = "" + "" + "";
        int count = 0;

//
        String msgSvrId = contentValues.getAsString("msgSvrId");


        if (contentValues.containsKey("content") && type == 285212721 && contentValues.getAsInteger("flag") == 0) {
            //xml解码
            Class<?> classXml = XposedHelpers.findClassIfExists("com.tencent.mm.sdk.platformtools.SemiXml", loadPackageParam.classLoader);
            Object xmlResult = XposedHelpers.callStaticMethod(classXml, "decode", contentValues.getAsString("content"));

//            info = JSON.toJSONString(param.args[2]).replace(contentValues.getAsString("content"), "");

            String json_str = JSON.toJSONString(xmlResult);
            Log.d(TAG, String.format("hook db data msgSvrId = : %s", msgSvrId));
            Log.d(TAG, String.format("hook db data : %s , %s", "content", json_str));


            com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(json_str);


            for (int i = 0; i < 8; i++) {
                String idx = "";
                if (i != 0) idx = String.valueOf(i);
                if (jsonObject.containsKey(".msg.appmsg.mmreader.category.item" + idx + ".title")) {
                    String title = jsonObject.getString(".msg.appmsg.mmreader.category.item" + idx + ".title");
//                    String input = title;
                    ArrayList<String> list = new ArrayList<>();
                    list.add("校招");
                    list.add("校园招聘");
                    list.add("国企招聘");
                    list.add("招聘");
                    list.add("春招");
                    list.add("秋招");
                    list.add("校招");
                    list.add("社招");
                    list.add("求职");
                    list.add("六险二金");
                    list.add("国企招聘");
                    list.add("央企招聘");
                    list.add("央国企招聘");
                    list.add("国央企招聘");
                    list.add("事业编");
                    list.add("正式编");
                    list.add("毕业生");
                    list.add("往届可报");
                    list.add("编制");
                    list.add("招满");
                    list.add("笔试");
                    list.add("面试");
                    list.add("校聘");
                    list.add("岗位");
                    list.add("军队文职");
                    list.add("秋季招聘");

                    boolean is_contains = false;
                    for (String keyword : list) {
                        if (title.contains(keyword)) {
                            is_contains = true;
                            break;
                        }
                    }
                    if (!is_contains) continue;
                    count += 1;

                    String timestamp = jsonObject.getString(".msg.appmsg.mmreader.category.item" + idx + ".pub_time");
                    String url = jsonObject.getString(".msg.appmsg.mmreader.category.item" + idx + ".url");
                    accountName = jsonObject.getString(".msg.appmsg.mmreader.category.item" + idx + ".sources.source.name");
                    info += System.lineSeparator();
                    info += title;
                    info += System.lineSeparator();
                    info += url;

                } else {
                    break;
                }
            }

            info += System.lineSeparator();
            info += System.lineSeparator();
            info += System.lineSeparator();
            info += System.lineSeparator();
            info += System.lineSeparator();
            info += "来自：" + accountName;

            // 创建一个Date对象表示当前时间
            Date currentDate = new Date();
            // 创建SimpleDateFormat对象来定义时间的格式
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 使用SimpleDateFormat对象将Date对象格式化为指定的格式
            String formattedDate = dateFormat.format(currentDate);
            info += "      " + formattedDate;


//                    Map<String, String> data = SemiXml.decode(contentValues.getAsString(key));
//                    if (null != xmlResult) {
//                        String dataStr = JSON.toJSONString(xmlResult);
//                        if (null != dataStr && "" != dataStr && "null" != dataStr) {
//                            JSONObject json = new JSONObject();
//                            json.put("site", "wechat");
//                            json.put("data", dataStr);
//                            Log.d(TAG, "成功hook公众号push数据，开始保持微信会话流程.");
////                            String s = HttpUtils.doGet(SHI_CI);
////                            String s = "嘻嘻笑";
////                            Log.d(TAG, "随机古诗:" + s);
//                            // 微信群定时上报消息，保持活跃
//                            Log.d(TAG, String.format("hookDB get====================== data err : %s", info));
//                            pushMsg(loadPackageParam, "28036017663@chatroom", info);
//                        }
//                    }
        }
//        }
//        for (String key : contentValues.keySet()) {
//            try {
//                Log.d(TAG, String.format("hook db ===================================================data : %s", key));
//                if (!key.equals(CRUX_KEY)) {
//                    continue;
//                }
////                if (null != contentValues.get(key)) {
////
//
//            } catch (Exception e) {
//                Log.d(TAG, String.format("hookDB get data err : %s , %s", key, e));
//            }
//        }
        if (count > 0) {
            pushMsg(loadPackageParam, "filetransfer", info);
//            pushMsg(loadPackageParam, "28036017663@chatroom", info);//测试群
//            pushMsg(loadPackageParam, "34396196911@chatroom", info);//就业中心群
        }
    }

    private static void pushMsg(XC_LoadPackage.LoadPackageParam loadPackageParam, String strChatroomId, String strContent) {
        replyTextMessage(loadPackageParam, strContent, strChatroomId);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                replyTextMessage(loadPackageParam, strContent, strChatroomId);
//            }
//        }).start();
    }

    /**
     * weixin 8.0.19 64
     *
     * @param loadPackageParam
     * @param strContent
     * @param strChatroomId
     */
    private static void replyTextMessage(final XC_LoadPackage.LoadPackageParam loadPackageParam, String strContent, final String strChatroomId) {
        XposedBridge.log("准备回复消息内容：content:" + strContent + ",chatroomId:" + strChatroomId);
        Log.d(TAG, "准备回复消息内容：content:" + strContent + ",chatroomId:" + strChatroomId);

//        Log.e("SendMsgCgiFactory", "[execute] cgi is null. %s", Util.getStack());
        Class<?> vClass = XposedHelpers.findClassIfExists("ne0.d0", loadPackageParam.classLoader);
        //调用静态方法创建消息对象，传入接受者wxid wxid_w45qm2lqc1zt12
        Object o1 = XposedHelpers.callStaticMethod(vClass, "a", strChatroomId);
        //设置消息内容
        XposedHelpers.setObjectField(o1, "c", strContent);
        XposedHelpers.setIntField(o1, "d", 1);
        XposedHelpers.setIntField(o1, "h", 5);
        //{"A":0.0,"B":0.0,"C":1,"a":"wxid_w45qm2lqc1zt12","c":"叫爸爸","d":1,"e":0,"f":0,"h":5,"i":0,"j":"","k":0,"n":0,"o":0,"p":false,"q":false,"r":"","s":"","t":0,"u":0,"w":0,"x":false,"y":0}

        //发送消息，掉两次a方法
        Object a = XposedHelpers.callMethod(o1, "a");
        Object a1 = XposedHelpers.callMethod(a, "a");


        Log.d(TAG, String.format("测试锚点======================================" + JSON.toJSONString(o1)));

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookDB(lpparam);
    }


}
