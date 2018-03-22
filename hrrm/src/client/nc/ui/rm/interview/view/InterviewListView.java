package nc.ui.rm.interview.view;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import nc.hr.utils.PubEnv;
import nc.hr.utils.ResHelper;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UITable;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.ui.pub.beans.table.GroupableTableHeader;
import nc.ui.pub.bill.BillListData;
import nc.ui.pub.bill.BillListPanel;
import nc.ui.pub.bill.BillModel;
import nc.ui.pub.bill.BillTableCellRenderer;
import nc.ui.rm.pub.view.DescriptionPanel;
import nc.ui.uif2.AppEvent;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.hr.tools.pub.GeneralVO;
import nc.vo.ic.m4460.entity.StateAdjustVO;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.bill.BillTempletBodyVO;
import nc.vo.pub.bill.BillTempletVO;
import nc.vo.pub.lang.UFLiteralDate;
import nc.vo.rm.interview.AggInterviewVO;
import nc.vo.rm.interview.IVPlanStateEnum;
import nc.vo.rm.interview.InterviewPlanVO;
import nc.vo.rm.interview.InterviewVO;
import nc.vo.rm.interview.IvReslutEnum;
import nc.vo.rm.psndoc.common.RMApplyTypeEnum;
import nc.vo.trade.voutils.VOUtil;
import org.apache.commons.lang.ArrayUtils;

public class InterviewListView extends InterviewBaseListView {

	private static final String InterviewVO = null;

	public InterviewListView() {
	}

	protected UIPanel createDescriptionPanel() {
		DescriptionPanel descPanel = new DescriptionPanel();
		descPanel.setLeadString(ResHelper.getString("6021interview",
				"06021interview0037"));

		JLabel jLabel = new JLabel();
		jLabel.setIcon(new ImageIcon(getClass().getResource(
				"/hr/images/icons/rmpass.png")));

		descPanel.add(jLabel,
				ResHelper.getString("6021interview", "06021interview0038"));

		JLabel fLabel = new JLabel();
		fLabel.setIcon(new ImageIcon(getClass().getResource(
				"/hr/images/icons/rmnotpass.png")));

		descPanel.add(fLabel,
				ResHelper.getString("6021interview", "06021interview0039"));

		JLabel iLabel = new JLabel();
		iLabel.setIcon(new ImageIcon(getClass().getResource(
				"/hr/images/icons/rmiv.png")));

		descPanel.add(iLabel,
				ResHelper.getString("6021interview", "06021interview0052"));

		JLabel hLabel = new JLabel();
		hLabel.setIcon(new ImageIcon(getClass().getResource(
				"/hr/images/icons/rmnoiv.png")));

		descPanel.add(hLabel,
				ResHelper.getString("6021interview", "06021interview0072"));

		descPanel.add(Color.orange,
				ResHelper.getString("6021psndoc", "06021psndoc0032"));
		descPanel.add(Color.WHITE,
				ResHelper.getString("6021psndoc", "06021psndoc0033"));
		descPanel.init();
		return descPanel;
	}

	public void initPageInfo() {
		
		int showOrder = 1000;
		BillTempletVO billTempletVO = getBillListPanel().getBillListData()
				.getBillTempletVO();
		List<BillTempletBodyVO> itemList = new ArrayList();
		itemList.addAll(Arrays.asList(this.bodyVOs));

		String[] headInfo = getHeadRoundKey();
		BillTempletBodyVO bodvo = (BillTempletBodyVO) this.bodyVOs[0].clone();

		bodvo.setListshowflag(Boolean.valueOf(false));
		bodvo.setShowflag(Boolean.valueOf(false));
		bodvo.setDatatype(Integer.valueOf(0));
		bodvo.setDefaultshowname("hidden");
		bodvo.setItemkey("hidden");
		bodvo.setPos(Integer.valueOf(0));
		bodvo.setMetadatapath(null);
		bodvo.setMetadataproperty(null);
		bodvo.setList(false);
		bodvo.setListflag(Boolean.valueOf(true));
		bodvo.setShoworder(Integer.valueOf(showOrder++));
		bodvo.setWidth(Integer.valueOf(1));
		itemList.add(bodvo);

		for (int i = 0; i < this.maxRound; i++) {
			BillTempletBodyVO bodyvo = (BillTempletBodyVO) this.bodyVOs[0]
					.clone();
			bodyvo.setListshowflag(Boolean.valueOf(true));
			bodyvo.setShowflag(Boolean.valueOf(true));
			bodyvo.setDatatype(Integer.valueOf(0));
			bodyvo.setDefaultshowname(getRoundNum(i + 1));
			bodyvo.setItemkey(headInfo[i]);
			bodyvo.setPos(Integer.valueOf(0));
			bodyvo.setMetadatapath(null);
			bodyvo.setMetadataproperty(null);
			bodyvo.setList(true);
			bodyvo.setListflag(Boolean.valueOf(true));
			bodyvo.setShoworder(Integer.valueOf(showOrder++));
			bodyvo.setWidth(Integer.valueOf(1));
			itemList.add(bodyvo);
		}

		BillTempletBodyVO bodyvo = (BillTempletBodyVO) this.bodyVOs[0].clone();
		bodyvo.setListshowflag(Boolean.valueOf(true));
		bodyvo.setShowflag(Boolean.valueOf(true));
		bodyvo.setDatatype(Integer.valueOf(0));
		bodyvo.setDefaultshowname(ResHelper.getString("6021interview",
				"06021interview0068"));
		bodyvo.setItemkey("waittime");
		bodyvo.setPos(Integer.valueOf(0));
		bodyvo.setMetadatapath(null);
		bodyvo.setMetadataproperty(null);
		bodyvo.setList(true);
		bodyvo.setListflag(Boolean.valueOf(true));
		bodyvo.setShoworder(Integer.valueOf(showOrder++));
		bodyvo.setWidth(Integer.valueOf(1));

		itemList.add(bodyvo);

		
		remove(getBillListPanel());
		billTempletVO
				.setChildrenVO((CircularlyAccessibleValueObject[]) itemList
						.toArray(new BillTempletBodyVO[0]));
		
		getBillListPanel().setListData(new BillListData(billTempletVO));
		
	

		this.billListPanel.getHeadTable().setSortEnabled(false);

		GroupableTableHeader header = (GroupableTableHeader) getBillListPanel()
				.getHeadTable().getTableHeader();

		header.addColumnGroup(getColumnGroup(headInfo));
		
		add(getBillListPanel());
	}

	public void handleEvent(AppEvent event) {
		super.handleEvent(event);
		 //设置复选框属性
	    getBillListPanel().setParentMultiSelect(true);
	    
	    setMultiSelectionEnable(true);
	    //界面出现复选框
	    setMultiSelectionMode(1);
	    //设置多选数据
	    setListMultiProp();
		if ("Model_Initialized".equalsIgnoreCase(event.getType())) {
			TableColumnModel columnModel = this.billListPanel.getHeadTable()
					.getColumnModel();
			columnModel.getColumn(columnModel.getColumnCount() - 1)
					.setCellRenderer(new BillTableCellRenderer() {
						public Component getTableCellRendererComponent(
								JTable table, Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {
							Component cmp = super
									.getTableCellRendererComponent(table,
											value, isSelected, hasFocus, row,
											column);

							BillModel billModel = (BillModel) table.getModel();
							DefaultConstEnum defaultEnum = (DefaultConstEnum) billModel
									.getValueObjectAt(row,
											"pk_psndoc.applytype");
							Integer typeValue = Integer.valueOf(defaultEnum == null ? 1
									: ((Integer) defaultEnum.getValue())
											.intValue());
							if (RMApplyTypeEnum.INAPPLY.toIntValue() == typeValue
									.intValue()) {
								setBackground(Color.orange);
							}
							Object obj = billModel.getValueObjectAt(row,
									"waittime");
							if (obj == null) {
								return cmp;
							}
							if (2 < ((Integer) billModel.getValueObjectAt(row,
									"waittime")).intValue()) {
								setForeground(Color.RED);
							}
							return cmp;
						}
					});
			//设置字段排序
			this.billListPanel.getHeadTable().setSortEnabled(true);
			this.billListPanel.updateUI();
		}
	}

	public GeneralVO[] getShowValues() {
		Object[] objs = getModel().getData().toArray();
		if (ArrayUtils.isEmpty(objs)) {
			return null;
		}
		List<GeneralVO> resultList = new ArrayList();
		List<String> strConApp = new ArrayList();
		for (int j = 0; j < objs.length; j++) {
			Object obj = objs[j];
			AggInterviewVO aggVO = (AggInterviewVO) obj;
			InterviewVO headVO = aggVO.getInterviewVO();
			GeneralVO vo = new GeneralVO();
			resultList.add(vo);

			String[] names = headVO.getAttributeNames();
			for (String name : names) {
				if (!name.equals("pk_psndoc_job")) {
					if (name.equals("pk_reg_dept")) {
						if (strConApp.contains(headVO.getPk_reg_dept()
								+ headVO.getPk_reg_job())) {
							vo.setAttributeValue("hidden", "Y");
							vo.setStatus(0);
						} else {
							vo.setAttributeValue("hidden", "N");
						}
						strConApp.add(headVO.getPk_reg_dept()
								+ headVO.getPk_reg_job());
						vo.setAttributeValue("pk_reg_dept",
								headVO.getPk_reg_dept());
						vo.setAttributeValue("pk_psndoc_job",
								headVO.getPk_psndoc_job());
					} else {
						vo.setAttributeValue(name,
								headVO.getAttributeValue(name));
					}
				}
			}
//			InterviewPlanVO[] bodyvos = aggVO.getInterviewPlanVOs();
//			int bodyLength = ArrayUtils.getLength(bodyvos);
//			VOUtil.sort(bodyvos, new String[] { "roundnum" }, new int[] { 1 });
//			String[] headInfo = getHeadRoundKey();
//			for (int i = 0; i < headInfo.length; i++) {
//				Integer objValue = i >= bodyLength ? Integer.valueOf(-1)
//						: bodyvos[i].getResult();
//				if ((objValue != null)
//						&& (objValue.intValue() != -1)
//						&& (IvReslutEnum.WAIT.toIntValue() != objValue
//								.intValue())
//						&& (bodyvos[i].getIvstate() != null)
//						&& (bodyvos[i].getIvstate().intValue() == IVPlanStateEnum.SAVED
//								.toIntValue())) {
//					vo.setAttributeValue(headInfo[i],
//							Integer.valueOf(IvReslutEnum.WAIT.toIntValue()));
//				} else {
//					vo.setAttributeValue(headInfo[i], objValue);
//				}
//			}
//			for (int i = 0; i < bodyvos.length; i++) {
//				if ((bodyvos[i].getBegindate() != null)
//						&& (bodyvos[i].getEnddate() == null)) {
//					vo.setAttributeValue("waittime", Integer.valueOf(PubEnv
//							.getServerLiteralDate().getDaysAfter(
//									bodyvos[i].getBegindate())));
//				}
//			}
		}
		return (GeneralVO[]) resultList.toArray(new GeneralVO[0]);
	}

}
