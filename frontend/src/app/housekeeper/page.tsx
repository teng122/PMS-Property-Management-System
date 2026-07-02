"use client";

import { useState } from "react";
import { Sparkles, Play, Check } from "lucide-react";
import {
    useDirtyRooms,
    useStartCleaning,
    useCompleteCleaning,
} from "@/hooks/useHousekeeping";

import {
    Button,
    Card,
    CardBody,
    LoadingBlock,
    ErrorBlock,
    EmptyBlock,
} from "@/components/ui";

import { errorMessage } from "@/lib/utils";

type TabType = "ASSIGNED" | "IN_PROGRESS";

export default function HousekeepingPage() {
    const [tab, setTab] = useState<TabType>("ASSIGNED");

    const { data = [], isLoading, isError, error } = useDirtyRooms();
    const start = useStartCleaning();
    const complete = useCompleteCleaning();

    // split tasks by status
    const assignedTasks = data.filter((t) => t.status === "ASSIGNED");
    const inProgressTasks = data.filter((t) => t.status === "IN_PROGRESS");

    const tasksToShow =
        tab === "ASSIGNED" ? assignedTasks : inProgressTasks;

    return (
        <div className="space-y-4 p-4">

            {/* Header */}
            <div className="flex items-center gap-2">
                <Sparkles className="h-6 w-6 text-green-600" />
                <h1 className="text-xl font-semibold">Housekeeping</h1>
            </div>

            {/* Tabs */}
            <div className="flex gap-2 border-b pb-2">
                <button
                    onClick={() => setTab("ASSIGNED")}
                    className={`px-3 py-1 rounded-md text-sm ${
                        tab === "ASSIGNED"
                            ? "bg-blue-600 text-white"
                            : "bg-gray-100"
                    }`}
                >
                    Assigned ({assignedTasks.length})
                </button>

                <button
                    onClick={() => setTab("IN_PROGRESS")}
                    className={`px-3 py-1 rounded-md text-sm ${
                        tab === "IN_PROGRESS"
                            ? "bg-green-600 text-white"
                            : "bg-gray-100"
                    }`}
                >
                    In Progress ({inProgressTasks.length})
                </button>
            </div>

            {/* Loading */}
            {isLoading && <LoadingBlock />}

            {/* Error */}
            {isError && (
                <ErrorBlock
                    message={errorMessage(
                        error,
                        "Không kết nối được housekeeping-service"
                    )}
                />
            )}

            {/* Empty */}
            {!isLoading && tasksToShow.length === 0 && (
                <EmptyBlock message="Không có task trong tab này." />
            )}

            {/* Task list */}
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {tasksToShow.map((task) => (
                    <Card key={task.id}>
                        <CardBody className="space-y-2">

                            <div className="text-xs text-gray-500">Task ID</div>
                            <div className="font-mono text-xs">{task.id}</div>

                            <div className="text-sm">
                                Room:{" "}
                                <span className="font-mono text-xs">
                  {task.roomId}
                </span>
                            </div>

                            <div className="text-sm font-medium">
                                Status: {task.status}
                            </div>

                            {/* Actions */}
                            <div className="flex gap-2 pt-2">

                                {/* ASSIGNED → Start */}
                                {tab === "ASSIGNED" && (
                                    <Button
                                        className="flex-1"
                                        loading={
                                            start.isPending &&
                                            start.variables === task.id
                                        }
                                        onClick={() => start.mutate(task.id)}
                                    >
                                        <Play className="h-4 w-4" />
                                        Start
                                    </Button>
                                )}

                                {/* IN_PROGRESS → Complete */}
                                {tab === "IN_PROGRESS" && (
                                    <Button
                                        variant="success"
                                        className="flex-1"
                                        loading={
                                            complete.isPending &&
                                            complete.variables === task.id
                                        }
                                        onClick={() => complete.mutate(task.id)}
                                    >
                                        <Check className="h-4 w-4" />
                                        Complete
                                    </Button>
                                )}

                            </div>

                        </CardBody>
                    </Card>
                ))}
            </div>

            {/* Mutation errors */}
            {(start.isError || complete.isError) && (
                <ErrorBlock
                    message={errorMessage(start.error ?? complete.error)}
                />
            )}
        </div>
    );
}