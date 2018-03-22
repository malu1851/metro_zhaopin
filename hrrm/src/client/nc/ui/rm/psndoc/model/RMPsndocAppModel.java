package nc.ui.rm.psndoc.model;

import org.apache.commons.lang.StringUtils;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.rm.IRMPsndocQueryService;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.bd.meta.IBDObject;
import nc.vo.pub.BusinessException;
import nc.vo.rm.psndoc.AggRMPsndocVO;
import nc.vo.rm.psndoc.common.RMApplyStatusEnum;

public class RMPsndocAppModel extends BillManageModel {
	public void syncPhoto(AggRMPsndocVO selectData){
		if (selectData == null || StringUtils.isEmpty(selectData.getPsndocVO().getPk_psndoc())) {
			return;
		}
		if (selectData.getPsndocVO().getPhoto() != null) {
			return;
		}
		try {
		
			String pkPsndoc = selectData.getPsndocVO().getPk_psndoc();
			AggRMPsndocVO psndocVO = NCLocator.getInstance().lookup(IRMPsndocQueryService.class).queryByPK(pkPsndoc);
			if (psndocVO == null) {
				return;
			}
			selectData.getPsndocVO().setPhoto(psndocVO.getPsndocVO().getPhoto());
		} catch (BusinessException e) {
			Logger.error(e.getMessage());
		}
		
	}
	private boolean isCardShow = false;

	public boolean isCardShow() {
		return isCardShow;
	}

	public void setCardShow(boolean isCardShow) {
		this.isCardShow = isCardShow;
	}
	
	/**
	 * 在应聘登记中新增再聘人员时，使用的再聘标志量
	 */
	private String reApplyFromRM;
	

	public String getReApplyFromRM() {
		return reApplyFromRM;
	}

	public void setReApplyFromRM(String reApplyFromRM) {
		this.reApplyFromRM = reApplyFromRM;
	}

	@Override
	public Object update(Object object) throws Exception {
		Object obj = getService().update(object);
		if(obj!=null&&((AggRMPsndocVO)obj).getPsndocVO().getApplystatus()==RMApplyStatusEnum.INIT.toIntValue())
			return obj;
		if(findBusinessData(obj)>=0)
			directlyUpdate(obj);
		else 
			directlyAdd(obj);
		return obj;
	}

	@Override
	public void directlyAdd(Object obj) {
		if(obj!=null&&((AggRMPsndocVO)obj).getPsndocVO().getApplystatus()==RMApplyStatusEnum.INIT.toIntValue())
			return;
		super.directlyAdd(obj);
	}
	
	public int findBusinessData(Object obj) {
		
		IBDObject target = getBusinessObjectAdapterFactory().createBDObject(obj);
		if(target==null)
			return -1;
		String targetId = (String) target.getId();
		if(datapks.contains(targetId))
			return datapks.indexOf(targetId);
		int i=0;
		for (Object object : getData()) {
			IBDObject tmp =  getBusinessObjectAdapterFactory().createBDObject(object);
			if(tmp.getId().equals(targetId))
				return i; 
			else
				i++;
		}
		return -1;
	}
}