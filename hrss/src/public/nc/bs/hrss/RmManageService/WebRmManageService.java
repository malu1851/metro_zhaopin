package nc.bs.hrss.RmManageService;

import nc.vo.pub.BusinessException;

public interface WebRmManageService {
	
    public Object[] RmResume(String key) throws BusinessException;
    
    public Object[] CountRmResume(String key) throws BusinessException;
    
    public Object[] RmStatu(String psndoc) throws BusinessException;

}
