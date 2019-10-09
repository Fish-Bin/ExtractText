package com.fish.bin.action;
import com.fish.bin.api.GetTranslationTask;
import com.fish.bin.bean.DataBean;
import com.fish.bin.utils.StringUtils;
import com.intellij.notification.impl.NotificationsManagerImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * 将xml中字符串到strings中
 *
 * @author liubin
 */
public class ExtractText extends AnAction {

    private String layoutName;
    private List<DataBean> dataBeans;
    private Editor editor;
    private Project project;
    private Document document;


    @Override
    public void actionPerformed(AnActionEvent e) {
        //当前项目
        project = e.getData(PlatformDataKeys.PROJECT);
        //当前文件
        PsiFile psiFile = e.getData(PlatformDataKeys.PSI_FILE);
        //当前光标位置
        editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && psiFile != null) {
            if (psiFile.getParent() != null && psiFile.getParent().getName().equals("layout") && psiFile.getName().contains("xml")) { //选中layout中的布局文件
                layoutName = psiFile.getName().split("\\.")[0];
                handLayout(layoutName);
            } else if (editor != null) { //选中java类的布局文件
                //可操作的文档文件
                document = editor.getDocument();
                layoutName = editor.getSelectionModel().getSelectedText();
                handLayout(layoutName);
            } else {
                showDialog("请选中布局文件", 2);
            }
        } else {
            showDialog("请选中焦点或布局文件", 2);
        }
    }

    /**
     * 处理布局，获取需要的内容
     *
     * @param layoutName 布局的名字
     */
    private void handLayout(String layoutName) {
        System.out.println("需要处理的布局文件为:" + layoutName);
        //获取布局文件
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, layoutName + ".xml", GlobalSearchScope.projectScope(project));
        //如果布局文件有且仅有一个
        if (filesByName.length == 1) {
            try {
                //文件内容
                PsiFile file = filesByName[0];
                //打印文件内容
                System.out.println(file.getText());
                //储存需要的内容
                dataBeans = new LinkedList<>();
                List<String> soureces = new ArrayList<>();
                //xml解析,获取所有标签元素(不处理include标签)
                file.accept(new XmlRecursiveElementVisitor(true) {
                    @Override
                    public void visitXmlTag(XmlTag tag) {
                        super.visitXmlTag(tag);
                        //获取标签以及value值
                        String valueText = tag.getAttributeValue("android:text");
                        String valueHint = tag.getAttributeValue("android:hint");
                        if (valueText != null && !valueText.isEmpty() && !valueText.contains("@string")) {
                            soureces.add(valueText);
                            String key = layoutName + "_";
                            dataBeans.add(new DataBean(key, valueText));
                        }
                        if (valueHint != null && !valueHint.isEmpty() && !valueHint.contains("@string")) {
                            soureces.add(valueHint);
                            String key = layoutName + "_";
                            dataBeans.add(new DataBean(key, valueHint));
                        }
                    }
                });
                if (soureces.size() == 0) {
                    showDialog("没有需要替换的字符串", 1);
                } else {
                    //翻译并处理结果
                    translate(file, soureces);
                }
            } catch (Exception e) {
                showDialog("全局异常：" + e.getMessage(), 2);
            }
        } else {
            showDialog("请选中layout", 2);
        }
    }

    private void handResult(PsiFile file) {
        if (dataBeans.size() > 0) {
            //改文件的值
            changeXml(file);
            //获取内容
            String content = getShowContent(dataBeans);
            //往xml中写内容
            writeContentXml(content);
            //显示文字(弹出框形式)
            showDialog("success", 2);
        } else {
            showDialog("没有需要提取的字符串", 2);
        }
    }

    /**
     * 翻译
     */
    private void translate(PsiFile file, List<String> sources) {
        new GetTranslationTask(project, "翻译中", sources, result -> {
            if (result != null) {
                for (int i = 0; i < dataBeans.size(); i++) {
                    DataBean dataBean = dataBeans.get(i);
                    dataBean.setKey(dataBean.getKey() + StringUtils.formatStr(result.get(i)));
                }
                handResult(file);
            } else {
                showDialog("翻译异常", 2);
            }
        }).setCancelText("Translation Has Been Canceled").queue();
    }


    /**
     * 改布局文件里的映射
     */
    private void changeXml(PsiFile file) {
        Runnable runnable = () -> {
            VirtualFile virtualFile = file.getVirtualFile();
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if(document!=null){
                String text = document.getText();
                for (DataBean attributeValue : dataBeans) {
                    text = text.replace("\"" + attributeValue.getValue() + "\"", "\"" + "@string/" + attributeValue.getKey() + "\"");
                }
                document.setText(text);
            }
        };
        WriteCommandAction.runWriteCommandAction(project, runnable);
    }

    /**
     * 往strings.xml中写内容
     */
    private void writeContentXml(String content) {
        try {
            String stringPath = project.getBasePath() + "/app/src/main/res/values/strings.xml";
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(stringPath);
            if (virtualFile != null) {
                Runnable runnable = () -> {
                    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
                    if(document!=null){
                        int lineCount = document.getLineCount();
                        int lineNumber = document.getLineStartOffset(lineCount - 1);
                        document.insertString(lineNumber, content);
                    }
                };
                WriteCommandAction.runWriteCommandAction(project, runnable);
            } else {
                showDialog("strings.xml文件没找到", 2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向文件中写入内容
     */
    private void writeContentCurrent(String content) {
        int maxOffset = document.getTextLength();
        Runnable runnable = () -> document.insertString(maxOffset, content);
        WriteCommandAction.runWriteCommandAction(project, runnable);
    }

    /**
     * 获取需要显示的内容
     */
    private String getShowContent(List<DataBean> attributeValues) {
        StringBuilder block = new StringBuilder();
        block.append("<!--################ ").append(layoutName).append(" start ################-->\n");
        for (DataBean dataBean : attributeValues) {
            block.append("<string name=\"")
                    .append(dataBean.getKey())
                    .append("\">")
                    .append(dataBean.getValue())
                    .append("</string>\n");
        }
        block.append("<!--################ ").append(layoutName).append(" end ################-->\n");
        return block.toString();
    }

    /**
     * 显示提示框
     */
    private void showDialog(final String result, final int time) {
        ApplicationManager.getApplication().invokeLater(() -> {
            JBPopupFactory factory = JBPopupFactory.getInstance();
            Balloon balloon = factory.createHtmlTextBalloonBuilder(result, null, JBColor.GRAY, null)
                    .setFadeoutTime(time * 1000)
                    .createBalloon();
            if (editor == null) {
                RelativePoint pointToShowPopup = null;
                IdeFrame window = (IdeFrame) NotificationsManagerImpl.findWindowForBalloon(project);
                if (window != null) pointToShowPopup = RelativePoint.getSouthEastOf(window.getComponent());
                balloon.show(pointToShowPopup, Balloon.Position.atLeft);
            } else {
                balloon.show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        });
    }

    /**
     * 显示文本框
     */
    private void showEditText(String result) {
        JBPopupFactory instance = JBPopupFactory.getInstance();
        instance.createDialogBalloonBuilder(new EditorTextField(EditorFactory.getInstance().createDocument(result), null, FileTypes.PLAIN_TEXT, false, false), "KViewBind-Generate")
                .setHideOnKeyOutside(true)
                .setHideOnClickOutside(true)
                .createBalloon()
                .show(instance.guessBestPopupLocation(editor), Balloon.Position.below);

    }
}
