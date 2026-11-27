import { useState, useCallback } from 'react';
import { Node, Edge } from 'reactflow';

interface HistoryState {
  nodes: Node[];
  edges: Edge[];
}

interface UndoRedoStore {
  history: HistoryState[];
  currentIndex: number;
}

const MAX_HISTORY_SIZE = 50;

const cloneSnapshot = (nodes: Node[], edges: Edge[]): HistoryState => ({
  nodes: JSON.parse(JSON.stringify(nodes)),
  edges: JSON.parse(JSON.stringify(edges)),
});

export const useUndoRedo = (
  setNodes: (nodes: Node[] | ((nodes: Node[]) => Node[])) => void,
  setEdges: (edges: Edge[] | ((edges: Edge[]) => Edge[])) => void
) => {
  const [store, setStore] = useState<UndoRedoStore>({
    history: [],
    currentIndex: -1,
  });

  const takeSnapshot = useCallback((nodes: Node[], edges: Edge[]) => {
    const snapshot = cloneSnapshot(nodes, edges);
    setStore((prev) => {
      const truncated = prev.history.slice(0, prev.currentIndex + 1);
      const nextHistory = [...truncated, snapshot];
      const boundedHistory =
        nextHistory.length > MAX_HISTORY_SIZE
          ? nextHistory.slice(nextHistory.length - MAX_HISTORY_SIZE)
          : nextHistory;
      return {
        history: boundedHistory,
        currentIndex: boundedHistory.length - 1,
      };
    });
  }, []);

  const undo = useCallback(() => {
    setStore((prev) => {
      if (prev.currentIndex <= 0) return prev;
      const prevIndex = prev.currentIndex - 1;
      const snapshot = prev.history[prevIndex];
      if (!snapshot) return prev;
      const cloned = cloneSnapshot(snapshot.nodes, snapshot.edges);
      setNodes(cloned.nodes);
      setEdges(cloned.edges);
      return { ...prev, currentIndex: prevIndex };
    });
  }, [setNodes, setEdges]);

  const redo = useCallback(() => {
    setStore((prev) => {
      if (prev.currentIndex >= prev.history.length - 1) return prev;
      const nextIndex = prev.currentIndex + 1;
      const snapshot = prev.history[nextIndex];
      if (!snapshot) return prev;
      const cloned = cloneSnapshot(snapshot.nodes, snapshot.edges);
      setNodes(cloned.nodes);
      setEdges(cloned.edges);
      return { ...prev, currentIndex: nextIndex };
    });
  }, [setNodes, setEdges]);

  const canUndo = store.currentIndex > 0;
  const canRedo = store.currentIndex < store.history.length - 1;

  return {
    takeSnapshot,
    undo,
    redo,
    canUndo,
    canRedo,
  };
};
