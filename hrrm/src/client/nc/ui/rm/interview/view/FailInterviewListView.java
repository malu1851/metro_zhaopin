package nc.ui.rm.interview.view;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import nc.hr.utils.ResHelper;
import nc.ui.pub.beans.UIPanel;
import nc.ui.rm.pub.view.DescriptionPanel;

public class FailInterviewListView
  extends InterviewBaseListView
{
  public FailInterviewListView() {}
  
  protected UIPanel createDescriptionPanel()
  {
    DescriptionPanel descPanel = new DescriptionPanel();
    descPanel.setLeadString(ResHelper.getString("6021interview", "06021interview0037"));
    

    JLabel jLabel = new JLabel();
    jLabel.setIcon(new ImageIcon(getClass().getResource("/hr/images/icons/rmpass.png")));
    
    descPanel.add(jLabel, ResHelper.getString("6021interview", "06021interview0038"));
    

    JLabel fLabel = new JLabel();
    fLabel.setIcon(new ImageIcon(getClass().getResource("/hr/images/icons/rmnotpass.png")));
    
    descPanel.add(fLabel, ResHelper.getString("6021interview", "06021interview0039"));
    

    JLabel hLabel = new JLabel();
    hLabel.setIcon(new ImageIcon(getClass().getResource("/hr/images/icons/rmnoiv.png")));
    
    descPanel.add(hLabel, ResHelper.getString("6021interview", "06021interview0072"));
    

    descPanel.add(Color.orange, ResHelper.getString("6021psndoc", "06021psndoc0032"));
    descPanel.add(Color.WHITE, ResHelper.getString("6021psndoc", "06021psndoc0033"));
    descPanel.init();
    return descPanel;
  }

    //重父类的方法，通过设置属性为true来启用复选框
	@Override
	public void initPageInfo() {

	//设置复选框属性
    getBillListPanel().setParentMultiSelect(true);
    
    setMultiSelectionEnable(true);
    //界面出现复选框
    setMultiSelectionMode(1);
    //设置多选数据
    setListMultiProp();
		
	}
  
  
}
