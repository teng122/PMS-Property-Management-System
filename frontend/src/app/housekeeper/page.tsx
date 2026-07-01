"use client";

import { Sparkles, Play, Check } from "lucide-react";
import { useDirtyRooms, useStartCleaning, useCompleteCleaning } from "@/hooks/useHousekeeping";
import { Button, Card, CardBody, LoadingBlock, ErrorBlock, EmptyBlock } from "@/components/ui";
import { errorMessage } from "@/lib/utils";

export default function HousekeepingPage() {
  const { data, isLoading, isError, error } = useDirtyRooms();
  const start = useStartCleaning();
  const complete = useCompleteCleaning();

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2">
        <Sparkles className="h-6 w-6 text-green-600" />
        <h1 className="text-xl font-semibold">Phòng cần dọn</h1>
      </div>

      <div className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-700">
        ⚠️ Danh sách lấy từ housekeeping-service (gọi trực tiếp <code>:8084</code>, chưa qua Gateway).
        Backend chưa tạo <code>cleaning_task</code> tự động khi check-out — nếu trống, cần seed task
        hoặc bổ sung API tạo task. Xem FRONTEND_PLAN.md §10.
      </div>

      {isLoading ? (
        <LoadingBlock />
      ) : isError ? (
        <ErrorBlock message={errorMessage(error, "Không kết nối được housekeeping-service (:8084)")} />
      ) : (data ?? []).length === 0 ? (
        <EmptyBlock message="Không có phòng nào chờ dọn." />
      ) : (
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {(data ?? []).map((task) => (
            <Card key={task.id}>
              <CardBody className="space-y-2">
                <div className="text-sm text-slate-500">Task</div>
                <div className="font-mono text-xs">{task.id}</div>
                <div className="text-sm">
                  Phòng (roomId): <span className="font-mono text-xs">{task.roomId}</span>
                </div>
                <div className="flex gap-2 pt-1">
                  <Button
                    className="flex-1"
                    loading={start.isPending && start.variables === task.id}
                    onClick={() => start.mutate(task.id)}
                  >
                    <Play className="h-4 w-4" /> Bắt đầu
                  </Button>
                  <Button
                    variant="success"
                    className="flex-1"
                    loading={complete.isPending && complete.variables === task.id}
                    onClick={() => complete.mutate(task.id)}
                  >
                    <Check className="h-4 w-4" /> Xong
                  </Button>
                </div>
              </CardBody>
            </Card>
          ))}
        </div>
      )}

      {(start.isError || complete.isError) && (
        <ErrorBlock message={errorMessage(start.error ?? complete.error)} />
      )}
    </div>
  );
}
