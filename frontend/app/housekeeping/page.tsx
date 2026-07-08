"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { DataTable } from "@/components/ui/data-table";
import { ErrorState, LoadingState } from "@/components/ui/states";
import { StatusBadge, statusLabel } from "@/components/ui/status-badge";
import { PageHeading } from "@/components/layout/page-heading";
import { ProtectedPage } from "@/components/layout/protected-page";
import { housekeepingApi } from "@/lib/api/services";
import { extractErrorMessage } from "@/lib/api/error";
import { dateTime } from "@/lib/format";
import { useAuthStore } from "@/stores/auth-store";
import type { CleaningTaskStatus } from "@/types/api";

const taskStatuses: Array<CleaningTaskStatus | ""> = ["PENDING", "IN_PROGRESS", "COMPLETED", ""];

export default function HousekeepingPage() {
  return (
    <ProtectedPage roles={["ADMIN", "RECEPTIONIST", "STAFF"]}>
      <HousekeepingContent />
    </ProtectedPage>
  );
}

function HousekeepingContent() {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<CleaningTaskStatus | "">("PENDING");
  const [mineOnly, setMineOnly] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const dirtyRooms = useQuery({ queryKey: ["dirty-rooms"], queryFn: housekeepingApi.dirtyRooms });
  const tasks = useQuery({
    queryKey: ["housekeeping-tasks", status, mineOnly, user?.id],
    queryFn: () => housekeepingApi.tasks({ status: status || undefined, staffId: mineOnly ? user?.id : undefined }),
    enabled: !!user
  });

  const update = useMutation({
    mutationFn: ({ id, action }: { id: string; action: "start" | "complete" }) =>
      action === "start" ? housekeepingApi.start(id) : housekeepingApi.complete(id),
    onSuccess: async (_data, variables) => {
      setMessage(variables.action === "start" ? "Đã bắt đầu dọn phòng." : "Đã hoàn thành dọn phòng.");
      await queryClient.invalidateQueries({ queryKey: ["housekeeping-tasks"] });
      await queryClient.invalidateQueries({ queryKey: ["dirty-rooms"] });
    }
  });

  return (
    <>
      <PageHeading title="Dọn phòng" description="Theo dõi phòng chờ dọn, việc đang làm và hoàn tất bàn giao phòng." />
      <div className="grid cols-2">
        <Card>
          <CardHeader title="Phòng chờ dọn" description="Các phòng cần được dọn trước khi sẵn sàng đón khách mới." />
          {dirtyRooms.isLoading ? <LoadingState /> : null}
          {dirtyRooms.isError ? <ErrorState message={extractErrorMessage(dirtyRooms.error)} /> : null}
          <DataTable
            rows={dirtyRooms.data || []}
            getRowKey={(room) => room.id}
            searchableText={(room) => `${room.roomNumber} ${room.roomId}`}
            columns={[
              { key: "room", header: "Phòng", render: (room) => <strong>{room.roomNumber || room.roomId.slice(0, 8)}</strong> },
              { key: "task", header: "Mã việc", render: (room) => <code>{room.id.slice(0, 8)}</code> },
              { key: "staff", header: "Nhân viên", render: (room) => room.staffId?.slice(0, 8) || "Chưa nhận" },
              {
                key: "action",
                header: "Thao tác",
                render: (room) => (
                  <Button disabled={update.isPending} onClick={() => update.mutate({ id: room.id, action: "start" })}>
                    Bắt đầu
                  </Button>
                )
              }
            ]}
          />
        </Card>

        <Card>
          <CardHeader title="Danh sách công việc" />
          <div className="toolbar">
            <select className="select" style={{ maxWidth: 220 }} value={status} onChange={(event) => setStatus(event.target.value as CleaningTaskStatus | "")}>
              {taskStatuses.map((item) => <option key={item || "ALL"} value={item}>{item ? statusLabel(item) : "Tất cả"}</option>)}
            </select>
            <label className="actions">
              <input type="checkbox" checked={mineOnly} onChange={(event) => setMineOnly(event.target.checked)} />
              <span>Việc của tôi</span>
            </label>
          </div>
          {message ? <div className="toast" style={{ marginBottom: 16 }}>{message}</div> : null}
          {tasks.isLoading ? <LoadingState /> : null}
          {tasks.isError ? <ErrorState message={extractErrorMessage(tasks.error)} /> : null}
          {update.isError ? <ErrorState message={extractErrorMessage(update.error)} /> : null}
          <DataTable
            rows={tasks.data || []}
            getRowKey={(task) => task.id}
            searchableText={(task) => `${task.roomNumber} ${task.status} ${task.staffId}`}
            columns={[
              { key: "room", header: "Phòng", render: (task) => <strong>{task.roomNumber || task.roomId.slice(0, 8)}</strong> },
              { key: "status", header: "Trạng thái", render: (task) => <StatusBadge status={task.status} /> },
              { key: "staff", header: "Nhân viên", render: (task) => task.staffId?.slice(0, 8) || "Chưa nhận" },
              { key: "updated", header: "Cập nhật", render: (task) => dateTime(task.updatedAt) },
              {
                key: "actions",
                header: "Thao tác",
                render: (task) => (
                  <div className="actions">
                    {task.status === "PENDING" ? <Button onClick={() => update.mutate({ id: task.id, action: "start" })}>Bắt đầu</Button> : null}
                    {task.status === "IN_PROGRESS" ? <Button variant="success" onClick={() => update.mutate({ id: task.id, action: "complete" })}>Hoàn tất</Button> : null}
                  </div>
                )
              }
            ]}
          />
        </Card>
      </div>
    </>
  );
}
