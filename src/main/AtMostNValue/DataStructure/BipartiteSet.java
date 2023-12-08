package main.AtMostNValue.DataStructure;

// This code was mostly written by Hadrien Cambazard and some by Frederic Berthiaume
public class BipartiteSet {
    public int[] list;

    public int[] position;

    public int last;

    public BipartiteSet(final int[] values){
        buildList(values);
    }

    public BipartiteSet(final int numberOfValues){
        int[] values = new int[numberOfValues];
        for(int i=0; i<numberOfValues;i++){
            values[i]=i;
        }
        buildList(values);
    }

    public void buildList(final int[] values){
        this.list = values;
        int max = 0;
        for(int i=0; i<values.length; i++){
            if(values[i]>max){
                max = values[i];
            }
        }
        this.position = new int[max+1];
        for(int i=0; i<=max; i++){
            position[values[i]]=i;
        }

        this.last=list.length-1;
    }

    public final int size(){return last+1;}

    public final boolean isEmpty(){return last==-1;}

    public final void clear(){last =-1;}

    public final boolean contain(final int value){return position[value] <= last;}

    public final void add(final int object){
        assert (!contain(object));
        final int idxToAdd  = position[object];
        final int idxToSwap = last + 1;
        final int temp = list[idxToSwap];
        list[idxToSwap] = object;
        list[idxToAdd] = temp;
        position[object] = idxToSwap;
        position[temp] = idxToAdd;
        last ++;
    }

    public final void remove(final int object){
        assert contain(object);
        final int idxToRemove  = position[object];
        final int idxToSwap = last;
        final int temp = list[idxToSwap];
        list[idxToSwap] = object;
        list[idxToRemove] = temp;
        position[object] = idxToSwap;
        position[temp] = idxToRemove;
        last --;
    }

    public final void removeLast(){remove(list[last]);}

    public final void full(){last = list.length-1;}

    public final int get(final int index){return list[index];}

    public final void addAll(BipartiteSet bs){
        for(int i=0; i<bs.last;i++){
            bs.add(bs.list[i]);
        }
    }

    public Integer[] toIntegerTable() {
        Integer[] table = new Integer[size()];
        for (int i = 0; i < table.length; i++) {
            table[i] = list[i];
        }
        return table;
    }

    public final int findIndexOfInt(final int a) {
        return list.length - position[a];
    }

    public boolean noBugInPosition(){
        boolean NoBug =true;
        for(int i=0;i<list.length;i++){
            NoBug &= list[position[i]]==i;
            if(list[position[i]]!=i){
                System.out.println("list[position["+i+"]]=list["+position[i]+"]="+list[position[i]]);
            }
        }
        return NoBug;
    }

    public final String pretty() {
        final StringBuilder s = new StringBuilder("[");
        for (int i = 0; i <= last; i++) {
            s.append(list[i]).append(i == (last) ? "" : ", ");
        }
        return s.append(']').toString();
    }

    public final String prettyFred() {
        final StringBuilder s = new StringBuilder("list     = [");
        for (int i = 0; i < list.length; i++) {
            s.append(list[i]).append(i == (last) ? "| " : i==list.length-1 ? "" :", ");
        }
        s.append("]\nposition = [").toString();
        for (int i = 0; i < position.length; i++) {
            s.append(position[i]).append(i == (last) ? "| " : i==position.length-1 ? "" :", ");
        }
        return s.append(']').toString();
    }



}
