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

    //�ظ���ķ�����ͨ����������Ϊtrue�����ø�ѡ��
	@Override
	public void initPageInfo() {

	//���ø�ѡ������
    getBillListPanel().setParentMultiSelect(true);
    
    setMultiSelectionEnable(true);
    //������ָ�ѡ��
    setMultiSelectionMode(1);
    //���ö�ѡ����
    setListMultiProp();
		
	}
  
  
}
