package com.borqs.syncml.ds.protocol;

import java.util.ArrayList;
import java.util.List;

import com.borqs.syncml.ds.imp.tag.TagItem;

/**
 * a new interface design for syncml pim operation
 * @author linxh
 *
 */
public interface IPimInterface2 {
    /**
     * get local data changes count
     * @return changes count
     */
    public int getChangedItemCount();
    
    public ArrayList<Long> getNewList();
    
    public ArrayList<Long> getUpdateList();
    
    public ArrayList<Long> getDelList();    
        
    public ISyncItem genDelItem(long id);

    public ISyncItem genAddItem(long id);

    public ISyncItem genUpdateItem(long id);
    
    //server process result
    public boolean serverAdd(long id, long srcId);
    
    public boolean serverDel(long id);
    
    public boolean serverUpdate(long id);
    
    //for syncing operations
    public void deleteAllContent();
    
    public long add(TagItem item);
    
    public long[] batchAdd(List<TagItem> itemList);
    
    public boolean batchDelete(long[] key);
    
    public boolean update(long key, TagItem item);

    public boolean delete(long key);

    public boolean serverOperationExecute();
}
