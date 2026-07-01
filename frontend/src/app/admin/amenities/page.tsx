"use client";

import { useState } from "react";
import { Plus } from "lucide-react";
import { useAmenities, useCreateAmenity } from "@/hooks/useAmenities";
import { Button, Card, CardBody, CardHeader, Input, Label, Select, LoadingBlock, ErrorBlock, EmptyBlock } from "@/components/ui";
import { formatCurrency, errorMessage } from "@/lib/utils";
import type { AmenityType } from "@/types";

export default function AdminAmenitiesPage() {
  const { data, isLoading, isError, error } = useAmenities();
  const create = useCreateAmenity();

  const [form, setForm] = useState({ name: "", price: 0, type: "FOOD" as AmenityType });

  function submit(e: React.FormEvent) {
    e.preventDefault();
    create.mutate(form, { onSuccess: () => setForm({ name: "", price: 0, type: "FOOD" }) });
  }

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <Card className="lg:col-span-1 h-fit">
        <CardHeader className="font-medium">Thêm dịch vụ</CardHeader>
        <CardBody>
          <form onSubmit={submit} className="space-y-3">
            <div>
              <Label>Tên dịch vụ</Label>
              <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div>
              <Label>Giá (VND)</Label>
              <Input
                type="number"
                min={0}
                value={form.price}
                onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
                required
              />
            </div>
            <div>
              <Label>Loại</Label>
              <Select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value as AmenityType })}>
                <option value="FOOD">FOOD</option>
                <option value="LAUNDRY">LAUNDRY</option>
                <option value="SPA">SPA</option>
              </Select>
            </div>
            {create.isError && <ErrorBlock message={errorMessage(create.error)} />}
            <Button type="submit" className="w-full" loading={create.isPending}>
              <Plus className="h-4 w-4" /> Thêm
            </Button>
          </form>
        </CardBody>
      </Card>

      <div className="lg:col-span-2">
        <h2 className="mb-3 text-lg font-semibold">Menu dịch vụ</h2>
        {isLoading ? (
          <LoadingBlock />
        ) : isError ? (
          <ErrorBlock message={errorMessage(error)} />
        ) : (data ?? []).length === 0 ? (
          <EmptyBlock message="Chưa có dịch vụ nào." />
        ) : (
          <Card>
            <table className="w-full text-sm">
              <thead className="border-b bg-slate-50 text-left text-slate-500">
                <tr>
                  <th className="p-3">Tên</th>
                  <th className="p-3">Loại</th>
                  <th className="p-3 text-right">Giá</th>
                </tr>
              </thead>
              <tbody>
                {(data ?? []).map((a) => (
                  <tr key={a.id} className="border-b last:border-0">
                    <td className="p-3 font-medium">{a.name}</td>
                    <td className="p-3">{a.type}</td>
                    <td className="p-3 text-right">{formatCurrency(a.price)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>
        )}
      </div>
    </div>
  );
}
