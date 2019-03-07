package com.erictao2.crowbar.commmon.consistent.hash;

import com.google.common.hash.Hashing;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author TJX
 * @Title: ConsistentHashing
 * @date 2019/3/223:48
 */
public class ConsistentHash {

    private TreeMap<Integer, String> circle = new TreeMap<>();

    private int count = 100;


    public class Node {
        String ip;
        String key;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    private ConsistentHash(){

    }

    public ConsistentHash init(List<Node> nodes) {
        ConsistentHash consistentHash = new ConsistentHash();
        for (Node node : nodes) {
            consistentHash.put(node.getKey(), node.getIp());
        }
        return consistentHash;
    }

    /**
     * 添加节点
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        String[] virNodes = generateVirNodes(key, count);
        for (String virNode : virNodes) {
            int hash = murmur3Hash(virNode);
            if (circle.get(hash) == null) {
                circle.put(hash, value);
            } else {
                int hashPre = hash;
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    hash = hashPre + integerHash(i);
                    if (circle.get(hash) == null) {
                        circle.put(hash, value);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 需要存储的数据获取对应的节点
     * @param key
     * @return
     */
    public String get(String key) {
        int hash = murmur3Hash(key);
        Map.Entry<Integer, String> entry = circle.ceilingEntry(hash);
        return entry == null ? circle.firstEntry().getValue() : entry.getValue();
    }

    private String[] generateVirNodes(String key, int count) {
        String[] virNodes = new String[count];
        for (int i = 0; i < count; i++) {
            virNodes[i] = generateVirNode(key, i);
        }
        return virNodes;
    }

    private String generateVirNode(String key, int i) {
        return key + "##vir" + i;
    }

    /**
     * MurmurHash直接调了Guava里的实现
     * @param key
     * @return
     */
    private int murmur3Hash(String key) {
        byte[] bytes = key.getBytes();
        return Hashing.murmur3_32().hashBytes(bytes).asInt();
    }

    /**
     * 整型Hash
     * Thomas Wang's 32 bit Mix Function
     * @param key
     * @return
     */
    private int integerHash(int key) {
        key = ~key + (key << 15); // key = (key << 15) - key - 1;
        key = key ^ (key >>> 12);
        key = key + (key << 2);
        key = key ^ (key >>> 4);
        key = key * 2057; // key = (key + (key << 3)) + (key << 11);
        key = key ^ (key >>> 16);
        return key;
    }
}
